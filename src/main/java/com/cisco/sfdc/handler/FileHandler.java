package com.cisco.sfdc.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sforce.bulk.CsvWriter;
 
 
public class FileHandler{
			
		private static final Logger logger = LoggerFactory.getLogger(FileHandler.class);
	
        public static ArrayList<String> getResultLines(ArrayList<String> textData){
        ArrayList<String> resultLines = new ArrayList<String>();
        int i =0;
        boolean flag = false;
       
        for(int j = 0; j < textData.size(); j++){
                //if line has text on it,
                if(textData.get(j) != null && textData.get(j).length() > 0){
                        //if line is the start of a new record Standard object Id's start with "00" custom objects start with "a0"
                        if(textData.get(j).startsWith("\"0") || textData.get(j).startsWith("\"a")){
                                //adds line to array, substitutes separators with unique separators and removes quotes
                                flag = false;
                                resultLines.add(textData.get(j));
                                resultLines.set(i, resultLines.get(i).replaceAll("\",\"", "___"));
                                resultLines.set(i, resultLines.get(i).replaceAll("\"", ""));
                                i++;
                                //sets flag based on if line has come back to fix a line break
                                if(!flag)
                                        flag = true;
                        }
                        //if last line was a new record and this line isn't a new record, add to previous line
                        else if(flag == true && !((textData.get(j).startsWith("\"0")) || (textData.get(j).startsWith("\"a")))){
                                resultLines.set(i-1, resultLines.get(i-1) + textData.get(j));
                                resultLines.set(i-1, resultLines.get(i-1).replaceAll("\",\"", "___"));
                                resultLines.set(i-1, resultLines.get(i-1).replaceAll("\"", ""));
                        }
                        //case for header line
                        else{
                                resultLines.add(textData.get(j));
                                resultLines.set(i, resultLines.get(i).replaceAll("\",", "___"));
                                resultLines.set(i, resultLines.get(i).replaceAll("\"", ""));
                                i++;
                        }
                }
        }
       
        return resultLines;
    }
       
    public static String writeCSVFromStream(InputStream in, String object, String fileDir) throws IOException{
        //gets QueryResultStream from BulkExample and object name, creates BufferedReader and output file in specified folder
        //makes output folder if it doesnt exist
    	File mkdir = new File(fileDir);
        logger.info("Was new Output folder created? {}",mkdir.mkdirs());
        logger.info("Output Directory = {}", mkdir.toPath());
        BufferedReader read
           = new BufferedReader(new InputStreamReader(in));
        //writes csv file to output folder
        String resultCsvFileAbsolutePath = mkdir.getAbsolutePath()+"/"+object+".csv";
        logger.info("Result Output File = {}", resultCsvFileAbsolutePath);
        FileWriter csvFile = new FileWriter(resultCsvFileAbsolutePath);
        String line = "";
        ArrayList<String> textData = new ArrayList<String>();
        //makes array of results
        while((line = read.readLine()) != null){
                if(line.length()>0){
                        line.trim();
                        textData.add(line);
                }
        }
        //cleans results fixing description and new line problems
        ArrayList<String> resultLines = getResultLines(textData);
       
        //writes to csv
        CsvWriter writer = new CsvWriter(resultLines.get(0).split("___"),csvFile);
        for(int k = 1; k < resultLines.size();k++){
                String[] row = resultLines.get(k).split("___");
                writer.writeRecord(row);
        }
       
        //closes stream
        writer.endDocument();
        return resultCsvFileAbsolutePath;
    }
}