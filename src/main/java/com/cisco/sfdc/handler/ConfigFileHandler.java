package com.cisco.sfdc.handler;

import java.io.IOException;
import java.util.List;


public interface ConfigFileHandler {
	
	List<com.cisco.sfdc.common.QureyConfigaration> readConfigFile(String configFilePath) throws IOException;
}
