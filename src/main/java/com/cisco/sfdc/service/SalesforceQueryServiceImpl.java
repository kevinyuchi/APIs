package com.cisco.sfdc.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.api.BulkApi;
import com.cisco.sfdc.common.OauthTokens;
import com.cisco.sfdc.common.QureyConfigaration;
import com.cisco.sfdc.handler.ConfigFileHandler;
import com.cisco.sfdc.handler.SalesforceBulkAPIConfigFileHandlerImpl;
import com.cisco.sfdc.helper.SalesforceQueryHelper;
import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

@Service
public class SalesforceQueryServiceImpl implements QueryService{

	@Autowired ConfigFileHandler configHandler;
	@Autowired BulkApi bulk;

    private static Logger logger = LoggerFactory.getLogger(SalesforceQueryServiceImpl.class);

	public void query(String queryConfig) {
		List<QureyConfigaration> configList = null;
		logger.debug("queryConfig = {}",queryConfig);
		try{
			logger.debug("queryConfig = {}, and config file handler = {}",queryConfig,configHandler);
			configList = configHandler.readConfigFile(queryConfig);	
		}catch(IOException e){
			logger.error("Failed to read the configuration file.");
		}	
		
		if(configList == null || configList.size() == 0){
			logger.error("No configration file found!");
			return;
		}
		
		for(QureyConfigaration config : configList){
			logger.info("*****************UserName on Configuration File : {}******************",config.getUserName());
			OauthTokens token = fetchTokens(config);
			if(token == null){
				logger.error("Failed to read salesforce tokens.");
				return;
			}
			token.print();
			bulkQuery(token,config);
		}
	}
	
	/*Fetch the tokens from config file*/
	private OauthTokens fetchTokens(QureyConfigaration config){
		config.print();
//		User user = userDao.getUserByUserName(config.getUserName());
//		if(user == null){
//			logger.error("User {} doesnot exist.",config.getUserName());
//			return null;
//		}s
//		logger.info("User ID = {}, Org ID = {}", user.getId(), user.getFit().getScoreSrcId());
//		return sfTokenDao.getTokenByOrgId(user.getFit().getScoreSrcId());
		return null;
    }
	
    private void bulkQuery(OauthTokens token, QureyConfigaration config){
    	String soql = config.getQuery();
		try {
			
			/* test code to login2Salesforce by username and passwd+token*/
//			token = Login2Salesforce.login("kevin@everstring.com.dev3", "!11YuchilcAE0FBEw17J8bfC3ds4rg6pZ",true, token);
			
			soql = soql.toLowerCase();
			String sobject = null;
			String csv = null;
			
			if(soql.contains("from")){
				sobject = SalesforceQueryHelper.readSobjectTypeFromSOQL(soql);
				logger.info("Bulk Query Arguments : sobject = {}, soql = {}, output directory = {}", sobject, soql, config.getOutputDir());		
				csv = bulk.query(soql, config.getOutputDir(), token.getAuthToken(), token.getBaseUrl());
			}
			else{
				sobject = soql.replaceAll("\\s","");
				logger.info("Bulk Query Arguments All Columns: sobject = {}, output directory = {}", sobject, config.getOutputDir());		
				csv = bulk.queryAllColumns(config.getOutputDir(), token.getAuthToken(), token.getBaseUrl());
			}
			
			logger.info("Bulk Query Response CSV file locats at {}", csv);
		} catch (AsyncApiException e) {
			logger.error("AsyncApiException : {}",e.getMessage());
		} catch (ConnectionException e) {
			logger.error("ConnectionException : {}",e.getMessage());
		} catch (IOException e) {
			logger.error("IOException : {}",e.getMessage());
		} catch (InterruptedException e) {
			logger.error("InterruptedException : {}",e.getMessage());
		}
	}

}
