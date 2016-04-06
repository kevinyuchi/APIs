package com.cisco.sfdc.handler;

import java.io.IOException;
import java.util.List;

import com.cisco.sfdc.common.QureyConfigaration;


public interface ConfigFileHandler {
	
	List<QureyConfigaration> readConfigFile(String configFilePath) throws IOException;
}
