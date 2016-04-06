package com.cisco.sfdc.helper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.handler.FileHandler;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BatchStateEnum;
import com.sforce.async.BulkConnection;
import com.sforce.async.CSVReader;
import com.sforce.async.ConcurrencyMode;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.JobStateEnum;
import com.sforce.async.OperationEnum;
import com.sforce.async.QueryResultList;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

@Service
public class BulkApiHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(BulkApiHelper.class);
	
	@Autowired RestApiHelper restApiHelper;
	
	final int ASYNC_THREAD_SLEEP_TIME = 15000;	
	final String RESULT_FILE_TOKEN = "_BulkRequestResponse";
	final int MAX_BYTES_PER_BATCH = 10000000;
	final int MAX_ROWS_PER_BATCH = 10000;	
	
	private  JobInfo jobInfo;
	private  BatchInfo batchInfo;
	private  BulkConnection conn;

	
	/*Bulk api job and batch process*/
	public String[] processBulkQueryBatch(JobInfo job, BulkConnection bulkConnection, ByteArrayInputStream bout) throws AsyncApiException, InterruptedException{
				logger.info("Process the bulk query batch!");
				this.jobInfo = job;
				this.conn = bulkConnection;
		        batchInfo = conn.createBatchFromStream(jobInfo, bout);
		        logger.debug("Batch has been added to Job!");
				// check batch status 
				batchInfo = conn.getBatchInfo(jobInfo.getId(), batchInfo.getId());
		        logger.debug("Check batches status updates!");
				while(batchInfo.getState() != BatchStateEnum.Completed && batchInfo.getState() != BatchStateEnum.Failed ){
					logger.info("Salesforce is processing bulk request. \n batch info : {}", batchInfo);
					Thread.sleep(ASYNC_THREAD_SLEEP_TIME); // 15 sec
					batchInfo = conn.getBatchInfo(jobInfo.getId(),batchInfo.getId());
				}
				
				// process the batch result
				switch(batchInfo.getState()){
				case Completed:
					QueryResultList list = conn.getQueryResultList(
							jobInfo.getId(), batchInfo.getId());
					return list.getResult();
				case Failed:
				case NotProcessed:
				default:
					conn.closeJob(jobInfo.getId());
					logger.error("Bulk request failed. \n batch info  : {} ", batchInfo);
					throw new RuntimeException("Failed to get bulk batch info for jobId " + batchInfo.getJobId()
		            + " error - " + batchInfo.getStateMessage());					
				}
	}

	/*Output the bulk api query csv result to fileDir*/
	public String outputQueryResult(String[] queryResults, String fileDir , String sobject) throws IOException, AsyncApiException{
		// check query result
				String csv = null;
				if (queryResults != null) {
					for (String resultId : queryResults) {
						// grabs result stream and passes it to result csv file 
						csv = FileHandler.writeCSVFromStream(
								conn.getQueryResultStream(jobInfo.getId(),
										batchInfo.getId(), resultId), sobject, fileDir);
						// grabs results to ensure integrity
						conn.getQueryResultList(jobInfo.getId(), batchInfo.getId())
								.getResult();
					}
					conn.closeJob(jobInfo.getId());
				}
				return csv;
	}
	

	/**
	 * Create and upload batches using a CSV file. The file into the appropriate
	 * size batch files.
	 * 
	 * @param connection
	 *            Connection to use for creating batches
	 * @param jobInfo
	 *            Job associated with new batches
	 * @param csvFileName
	 *            The source file for batch data
	 */
	public List<BatchInfo> createBatchesFromCSVFile(
			BulkConnection conn, JobInfo jobInfo, String csv)
			throws IOException, AsyncApiException {
		this.jobInfo = jobInfo;
		this.conn = conn;
		List<BatchInfo> batchInfos = new ArrayList<BatchInfo>();
		BufferedReader rdr = new BufferedReader(new InputStreamReader(
				new FileInputStream(csv)));
		
		// read the CSV header row
		byte[] headerBytes = (rdr.readLine() + "\n").getBytes("UTF-8");
		int headerBytesLength = headerBytes.length;
		File tmpFile = File.createTempFile("bulkAPI", ".csv");

		// Split the CSV file into multiple batches
		try {
			FileOutputStream tmpOut = new FileOutputStream(tmpFile);
			int currentBytes = 0;
			int currentLines = 0;
			String nextLine;
			//Parser the csv file line by line 
			while ((nextLine = rdr.readLine()) != null) {
				byte[] bytes = (nextLine + "\n").getBytes("UTF-8");
				// Create a new batch when our batch size or line limit is reached
				if (currentBytes + bytes.length > MAX_BYTES_PER_BATCH
						|| currentLines > MAX_ROWS_PER_BATCH) {
					createBatch(tmpOut, tmpFile, batchInfos, conn,
							jobInfo);
					currentBytes = 0;
					currentLines = 0;
				}
				//add header to tmp csv file ( a new file in a new batch)
				if (currentBytes == 0) {
					tmpOut = new FileOutputStream(tmpFile);
					tmpOut.write(headerBytes);
					currentBytes = headerBytesLength;
					// currentLine = header line
					currentLines = 1;
				}
				tmpOut.write(bytes);
				currentBytes += bytes.length;
				currentLines++;
			}
			// Finished processing all rows
			// Create a final batch for any remaining data
			// == 1 , header line == 1.  Empty CSV file has header without content
			// > 1 , CSV file has content.  Create a batch on it , and add the batch to the job
			if (currentLines > 1) {
				createBatch(tmpOut, tmpFile, batchInfos, conn, jobInfo);
			}
		} finally {
			tmpFile.delete();
		}
		rdr.close();
		return batchInfos;
	}

	/**
	 * Create a batch by uploading the contents of the file. This closes the
	 * output stream.
	 * 
	 * @param tmpOut
	 *            The output stream used to write the CSV data for a single
	 *            batch.
	 * @param tmpFile
	 *            The file associated with the above stream.
	 * @param batchInfos
	 *            The batch info for the newly created batch is added to this
	 *            list.
	 * @param connection
	 *            The BulkConnection used to create the new batch.
	 * @param jobInfo
	 *            The JobInfo associated with the new batch.
	 */
	public void createBatch(FileOutputStream tmpOut, File tmpFile,
			List<BatchInfo> batchInfos, BulkConnection connection,
			JobInfo jobInfo) throws IOException, AsyncApiException {
		tmpOut.flush();
		tmpOut.close();
		FileInputStream tmpInputStream = new FileInputStream(tmpFile);
		try {
			BatchInfo batchInfo = connection.createBatchFromStream(jobInfo,
					tmpInputStream);
			logger.info("BatchInfo : {}", batchInfo);
			batchInfos.add(batchInfo);

		} finally {
			tmpInputStream.close();
		}
	}

	/**
	 * Gets the results of the operation and checks for errors.
	 */
	public String checkResults(List<BatchInfo> batchInfoList, String sobject,
			String dmlResultFileDir) throws AsyncApiException, IOException {
		String csv_response = null;
		// batchInfoList was populated when batches were created and submitted
		for (BatchInfo b : batchInfoList) {
			InputStream result = conn.getBatchResultStream(jobInfo.getId(),
					b.getId());
			CSVReader rdr = new CSVReader(result);
			List<String> resultHeader = rdr.nextRecord();
			int resultCols = resultHeader.size();

			List<String> row;
			while ((row = rdr.nextRecord()) != null) {
				Map<String, String> resultInfo = new HashMap<String, String>();
				for (int i = 0; i < resultCols; i++) {
					resultInfo.put(resultHeader.get(i), row.get(i));
				}
				boolean success = Boolean.valueOf(resultInfo.get("Success"));
				String id = resultInfo.get("Id");
				String error = resultInfo.get("Error");
				if (!success) {
					if(id != null){
						logger.error("Record : {}", id);
					}
					logger.error("Failed with error: {}", error);
				}
			}
			// output the result to CSV file
			InputStream resultToFile = conn.getBatchResultStream(jobInfo.getId(),b.getId());
			logger.info("dmlResultFileDir = {} , and expected file prefix = {}", dmlResultFileDir, sobject + "Results");
			csv_response = FileHandler.writeCSVFromStream(resultToFile, sobject + RESULT_FILE_TOKEN, dmlResultFileDir);
		}
		return csv_response;
	}

	public void closeJob(String jobId)
			throws AsyncApiException {
		JobInfo job = new JobInfo();
		job.setId(jobId);
		job.setState(JobStateEnum.Closed);
		conn.updateJob(job);
	}

	/**
	 * Wait for a job to complete by polling the Bulk API.
	 * 
	 * @param connection
	 *            BulkConnection used to check results.
	 * @param job
	 *            The job awaiting completion.
	 * @param batchInfoList
	 *            List of batches for this job.
	 * @throws AsyncApiException
	 */
	public void awaitCompletion(List<BatchInfo> batchInfoList) throws AsyncApiException {
		Set<String> incomplete = new HashSet<String>();
		for (BatchInfo bi : batchInfoList) {
			incomplete.add(bi.getId());
		}
		while (!incomplete.isEmpty()) {
			try {
				Thread.sleep(ASYNC_THREAD_SLEEP_TIME);
			} catch (InterruptedException e) {
				logger.error("InterruptedException : {}", e.getMessage());
			}
			logger.info("Awaiting results...{}", incomplete.size());
			BatchInfo[] statusList = conn.getBatchInfoList(jobInfo.getId())
					.getBatchInfo();
			for (BatchInfo b : statusList) {
				if (b.getState() == BatchStateEnum.Completed
						|| b.getState() == BatchStateEnum.Failed) {
					if (incomplete.remove(b.getId())) {
						logger.info("BATCH STATUS:\n{}", b);
					}
				}
			}
		}
	}

	public JobInfo createBulkProcessJob(OperationEnum bulkOp, String sobject, BulkConnection conn) throws AsyncApiException {
		JobInfo job = new JobInfo();
		job.setObject(sobject);
		job.setOperation(bulkOp);
		job.setContentType(ContentType.CSV);
		job = conn.createJob(job);
		logger.info("created job = {}", job);
		return job;
	}

	public String queryAllColumns(String sobject , String sessionId , String instanceUrl) throws IOException{
		assert restApiHelper != null;
		String[] columns = restApiHelper.getAllSobjectColumns(sobject, sessionId , instanceUrl);
		StringBuilder soql = new StringBuilder();
		soql.append("SELECT Id");
		for(String column : columns){
			soql.append(",");
			soql.append(column);
		}
		soql.append(" FROM ");
		soql.append(sobject);
		return soql.toString();
	}
	
	public void setRestApiHelper(RestApiHelper restHelper){
		this.restApiHelper = restHelper;
	}
}
