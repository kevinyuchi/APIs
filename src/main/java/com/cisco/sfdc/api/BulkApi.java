package com.cisco.sfdc.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.helper.BulkApiHelper;
import com.cisco.sfdc.helper.SalesforceQueryHelper;
import com.cisco.sfdc.util.ApiVersion;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BatchInfo;
import com.sforce.async.BulkConnection;
import com.sforce.async.ContentType;
import com.sforce.async.JobInfo;
import com.sforce.async.OperationEnum;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

@Service
public class BulkApi {

	private static final Logger logger = LoggerFactory.getLogger(BulkApi.class);

	@Autowired
	BulkApiHelper helper;

	private String soql;
	private String bulkCsvFileDir;
	private String bulkCsvFilePath;
	private String sobject;
	private String upsertKey;

	final String API_VERSION = ApiVersion.V_CURRENT;

	public String query(String soql, String outputResultDirectory, String accessToken, String instanceUrl)
			throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = SalesforceQueryHelper.readSobjectTypeFromSOQL(soql);
		this.soql = soql;
		this.bulkCsvFileDir = outputResultDirectory;
		return bulkOpertion(OperationEnum.query, accessToken, instanceUrl);
	}

	public String queryAllColumns(String outputResultDirectory, String accessToken, String instanceUrl)
			throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.soql = helper.queryAllColumns(sobject, accessToken, instanceUrl);
		this.bulkCsvFileDir = outputResultDirectory;
		return bulkOpertion(OperationEnum.query, accessToken, instanceUrl);
	}

	public String insert(String sobject, String insertCsvFilePath, String outputResultDirectory, String accessToken,
			String instanceUrl) throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.bulkCsvFileDir = outputResultDirectory;
		this.bulkCsvFilePath = insertCsvFilePath;
		return bulkOpertion(OperationEnum.insert, accessToken, instanceUrl);

	}

	public String update(String sobject, String updateCsvFilePath, String outputResultDirectory, String accessToken,
			String instanceUrl) throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.bulkCsvFileDir = outputResultDirectory;
		this.bulkCsvFilePath = updateCsvFilePath;
		return bulkOpertion(OperationEnum.update, accessToken, instanceUrl);

	}

	public String upsert(String sobject, String upsertKey, String upsertCsvFilePath, String outputResultDirectory,
			String accessToken, String instanceUrl)
			throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.upsertKey = upsertKey;
		this.bulkCsvFileDir = outputResultDirectory;
		this.bulkCsvFilePath = upsertCsvFilePath;
		return bulkOpertion(OperationEnum.upsert, accessToken, instanceUrl);

	}

	public String delete(String sobject, String deleteCsvFilePath, String outputResultDirectory, String accessToken,
			String instanceUrl) throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.bulkCsvFileDir = outputResultDirectory;
		this.bulkCsvFilePath = deleteCsvFilePath;
		return bulkOpertion(OperationEnum.delete, accessToken, instanceUrl);

	}

	public String hardDelete(String sobject, String hardDeleteCsvFilePath, String outputResultDirectory,
			String accessToken, String instanceUrl)
			throws AsyncApiException, ConnectionException, IOException, InterruptedException {
		this.sobject = sobject;
		this.bulkCsvFileDir = outputResultDirectory;
		this.bulkCsvFilePath = hardDeleteCsvFilePath;
		return bulkOpertion(OperationEnum.hardDelete, accessToken, instanceUrl);

	}

	/*
	 * @param dmlOperation Bulk api Dml operation enum
	 * 
	 * @param accessToken Salesforce oAuth 2 accessToken
	 * 
	 * @param instanceUrl Selesforce instance rest api endpoint
	 */
	private String bulkOpertion(OperationEnum bulkOp, String accessToken, String instanceUrl)
			throws AsyncApiException, ConnectionException, IOException, InterruptedException {

		BulkConnection conn = login(instanceUrl, accessToken);
		JobInfo bulkJob = createBulkApiJob(bulkOp, conn);
		switch (bulkOp) {
		case query:
			return processBulkQueryRequest(conn, bulkJob);
		case insert:
		case upsert:
		case update:
		case delete:
		case hardDelete:
			return processBulkReuqest(conn, bulkJob);
		default:
			return null;
		}
	}

	/**
	 * Create a Salesforce Bulk API connector.
	 * 
	 * @param instanceUrl
	 *            Salesforce POD url, from Oauth 2.0
	 * @param accessToken
	 *            Salesforce sessionId , from Oauth 2.o
	 * @return Bulk API Connector.
	 * @throws AsyncApiException,
	 *             ConnectionException
	 */
	public BulkConnection login(String instanceUrl, String accessToken) throws AsyncApiException, ConnectionException {
		logger.info("SalesforceServerUrl = {}, and SessionId = {}", instanceUrl, accessToken);
		String instanceId = accessToken.substring(0, accessToken.indexOf("!"));
		String soapEndpoint_PartnerWSDL = instanceUrl + "/services/Soap/u/" + API_VERSION + "/" + instanceId;
		ConnectorConfig config = new ConnectorConfig();
		config.setServiceEndpoint(soapEndpoint_PartnerWSDL);
		config.setSessionId(accessToken);
		/*
		 * The endpoint for the Bulk API service is the same as for the normal*
		 * SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
		 */
		String bulkRestEndpoint = soapEndpoint_PartnerWSDL.substring(0, soapEndpoint_PartnerWSDL.indexOf("Soap/"))
				+ "async/" + API_VERSION;
		config.setRestEndpoint(bulkRestEndpoint);
		// This should only be false when doing debugging.
		config.setCompression(true);
		// Set this to true to see HTTP requests and responses on stdout
		config.setTraceMessage(true);
		return new BulkConnection(config);
	}

	/**
	 * Process bulk api query request
	 * 
	 * @param bulkOp
	 *            Bulk process DB operation
	 * @param conn
	 *            BulkConnection used to create the new job.
	 * @return The JobInfo for the new job.
	 * @throws AsyncApiException
	 */
	private JobInfo createBulkApiJob(OperationEnum bulkOp, BulkConnection conn) throws AsyncApiException {
		JobInfo job = new JobInfo();
		job.setObject(this.sobject);
		job.setOperation(bulkOp);
		job.setContentType(ContentType.CSV);
		if (this.upsertKey != null) {
			job.setExternalIdFieldName(this.upsertKey);
		}
		job = conn.createJob(job);
		logger.info("created job = {}", job);
		return job;
	}

	/**
	 * Create a new job using the Bulk API.
	 * 
	 * @param jobInfo
	 *            Bulk query job information
	 * @param conn
	 *            BulkConnection used to monitor job/batch status.
	 * @return Salesforce bulk query result
	 * @throws AsyncApiException,
	 *             InterruptedException
	 * @throws IOException
	 */
	private String processBulkQueryRequest(BulkConnection conn, JobInfo jobInfo)
			throws AsyncApiException, InterruptedException, IOException {

		assert jobInfo.getId() != null;

		// process bulk query job
		jobInfo = conn.getJobStatus(jobInfo.getId());

		// create batches and add them to job
		logger.info("Bulk Query SQOL = {}", soql);
		ByteArrayInputStream bout = new ByteArrayInputStream(this.soql.getBytes());
		logger.debug("jobInfo = {}", jobInfo);
		logger.debug("conn = {}", conn);
		// logger.debug("bout = {}", bout);

		String[] queryResults = helper.processBulkQueryBatch(jobInfo, conn, bout);

		// output the bulk query result
		return helper.outputQueryResult(queryResults, this.bulkCsvFileDir, this.sobject);
	}

	/**
	 * Process bulk api Create,Update,Delete, HardDelete operation
	 * 
	 * @param bulkOp
	 *            Bulk process DB operation
	 * @param conn
	 *            BulkConnection used to create the new job.
	 * @return The JobInfo for the new job.
	 * @throws AsyncApiException
	 */
	private String processBulkReuqest(BulkConnection conn, JobInfo job) throws AsyncApiException, IOException {
		File file = new File(bulkCsvFilePath);
		bulkCsvFileDir = file.getParent();
		logger.info("Creating batches from CSV file , and the file locates at : {}, and parentDir : {}",
				bulkCsvFilePath, bulkCsvFileDir);
		List<BatchInfo> batchInfoList = helper.createBatchesFromCSVFile(conn, job, bulkCsvFilePath);
		logger.info("Closed the job");
		helper.closeJob(job.getId());
		logger.info("Checking batches status");
		helper.awaitCompletion(batchInfoList);
		logger.info("Bulk request is done, and the response is under parsering");
		return helper.checkResults(batchInfoList, sobject, bulkCsvFileDir);
	}

	public BulkApiHelper getHelper() {
		return helper;
	}

	public void setHelper(BulkApiHelper helper) {
		this.helper = helper;
	}

}
