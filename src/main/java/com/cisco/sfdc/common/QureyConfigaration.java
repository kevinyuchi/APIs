package com.cisco.sfdc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QureyConfigaration {
	
        private static Logger logger = LoggerFactory.getLogger(QureyConfigaration.class);
	    
	    private String userName;
		private String outputDir;
		private String query;
	
		public QureyConfigaration(String userName, String outputDir, String query) {
			this.userName = userName;
			this.outputDir = outputDir;
			this.query = query;
		
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getOutputDir() {
			return outputDir;
		}
		public void setOutputDir(String outputDir) {
			this.outputDir = outputDir;
		}
		public String getQuery() {
			return query;
		}
		public void setQuery(String query) {
			this.query = query;
		}
		public void print(){
			logger.info("Print Query Config : client_name = {}, output_dir ={}, query = {}", getUserName(), getOutputDir(), getQuery());
		}
}
