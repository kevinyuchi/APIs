package com.cisco.sfdc.util;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.sfdc.common.OauthTokens;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class Login2Salesforce{
	
        private static Logger logger = LoggerFactory.getLogger(Login2Salesforce.class);
	
		public static  OauthTokens login(String userId, String passwd, boolean isSandbox , OauthTokens tokens) throws FileNotFoundException, ConnectionException{

		        	 ConnectorConfig partnerConfig = new ConnectorConfig();
		             partnerConfig.setUsername(userId);
		             partnerConfig.setPassword(passwd);
		             partnerConfig.setCompression(true);
		             partnerConfig.setTraceFile("traceLogs.txt");
		             partnerConfig.setTraceMessage(true);
		             partnerConfig.setPrettyPrintXml(true);
		             if(isSandbox){
			             partnerConfig.setAuthEndpoint("https://test.salesforce.com/services/Soap/u/"+ApiVersion.V_CURRENT);
		             }
		             else{
			             partnerConfig.setAuthEndpoint("https://login.salesforce.com/services/Soap/u/"+ApiVersion.V_CURRENT);
		             }
		             /* Creating the connection automatically handles login and stores
		              the session id in partnerConfig*/
		             new PartnerConnection(partnerConfig);
		             /* When PartnerConnection is instantiated, a login is implicitly
		              executed and, if successful,
		              a valid session is stored in the ConnectorConfig instance.
		              Use this key to initialize a BulkConnection:*/
		             tokens.setAuthToken(partnerConfig.getSessionId());
		             String serviceUrl = partnerConfig.getServiceEndpoint();
		             tokens.setBaseUrl(serviceUrl.substring(0, serviceUrl.indexOf("/service")));
		             logger.debug("Login2Salesforce : token = {}, baseUrl = {}", tokens.getAuthToken(), tokens.getBaseUrl());
		             return tokens;
			
		}
}
