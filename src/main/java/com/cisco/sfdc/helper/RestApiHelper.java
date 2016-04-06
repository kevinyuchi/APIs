package com.cisco.sfdc.helper;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.api.RestApi;
import com.cisco.sfdc.util.ApiVersion;



@Service
public class RestApiHelper {
	
		@Autowired RestApi rest; 
		
		public boolean hasSobject(JSONObject sobjectDescribe, String sobjectName){
			JSONArray sobjects = sobjectDescribe.getJSONArray("sobjects");
			for(int i=0; i<sobjects.length(); i++){
				JSONObject sobject = sobjects.getJSONObject(i);
				if(sobject.getString("name").equalsIgnoreCase(sobjectName)){
					return true;
				}
			}
			return false;
		}
		
		public String[] getAllSobjectColumns(String sobject, String sessionId , String instanceUrl) throws IOException{
			JSONObject sampleRecord = getSampleSobjectRecord(sobject, sessionId, instanceUrl);
			sampleRecord.remove("attributes");
			sampleRecord.remove("Id");
			/*unsupported compond fields*/
			if(sampleRecord.has("GeocodeAccuracy")){
				sampleRecord.remove("GeocodeAccuracy");
			}
			if(sampleRecord.has("Latitude")){
				sampleRecord.remove("Latitude");
			}
			if(sampleRecord.has("Longitude")){
				sampleRecord.remove("Longitude");
			}
			if(sampleRecord.has("Address")){
				sampleRecord.remove("Address");
			}
			return allColumns(sampleRecord);
		}
		
		public JSONObject getSampleSobjectRecord(String sobject, String sessionId , String instanceUrl) throws IOException{
			String query = "Select+id+FROM+"+sobject+"+limit+1";
			String restEndpoint = queryRestPointBuilder(instanceUrl, query);
			JSONObject sampleRecord = rest.salesforceRestCall(sessionId, restEndpoint);
			if(sampleRecord.getInt("totalSize") == 1){
				String guid = sampleRecord.getJSONArray("records").getJSONObject(0).getString("Id");;
				restEndpoint = recordAccessRestPointBuilder(instanceUrl, guid, sobject);
				return rest.salesforceRestCall(sessionId, restEndpoint);
			}
			return null;
		}
		
		private String queryRestPointBuilder(String instanceUrl, String query) {
			StringBuilder endpoint = new StringBuilder();
			endpoint.append(instanceUrl);
			endpoint.append(rest.SERVICES);
			endpoint.append(ApiVersion.V_CURRENT);
			endpoint.append(rest.QUERY);
			endpoint.append(query);
			return endpoint.toString();
		}
		
		private String recordAccessRestPointBuilder(String instanceUrl, String GUID, String sobject) {
			StringBuilder endpoint = new StringBuilder();
			endpoint.append(instanceUrl);
			endpoint.append(rest.SERVICES);
			endpoint.append(ApiVersion.V_CURRENT);
			endpoint.append("/sobjects/");
			endpoint.append(sobject);
			endpoint.append("/");
			endpoint.append(GUID);
			return endpoint.toString();
		}

		public String[] allColumns(JSONObject sampleRecord){
			return JSONObject.getNames(sampleRecord);
		}
		
		public void setRestApi(RestApi rest){
			this.rest = rest;
		}
}
