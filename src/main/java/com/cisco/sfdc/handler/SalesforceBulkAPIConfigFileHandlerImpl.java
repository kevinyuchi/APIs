package com.cisco.sfdc.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.common.QureyConfigaration;


@Service
public class SalesforceBulkAPIConfigFileHandlerImpl implements ConfigFileHandler{

    private static Logger logger = LoggerFactory.getLogger(SalesforceBulkAPIConfigFileHandlerImpl.class);
    public final String USER_NAME = "user_name";
    public final String QUERY = "soql_query";
    public final String OUTPUT_DIR = "output_dir";

	/*Config file parser*/
	public List<QureyConfigaration> readConfigFile(String configFilePath) throws IOException {
		File configFile = new File(configFilePath);
		List<QureyConfigaration> configList = new ArrayList<QureyConfigaration>();
        BufferedReader br = new BufferedReader(new FileReader(configFile));
        String configStr = new String();
        do {
        	configStr = br.readLine();
            if(configStr == null){
            	break;
            }
            configList.add(readConfig(configStr));
        } while (configStr != null);
        br.close();
		return configList;
	}
	
	/*Config Interpreter*/
	public QureyConfigaration readConfig(String configStr) {
		JSONObject configJson = new JSONObject(configStr);
		QureyConfigaration config = new QureyConfigaration(
				configJson.getString(USER_NAME),
				configJson.getString(OUTPUT_DIR),
				configJson.getString(QUERY));
		return config;		
	}

}
