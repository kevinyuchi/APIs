package com.cisco.sfdc.util;

import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class HttpMethods {
    private static Logger LOGGER = LoggerFactory.getLogger(HttpMethods.class);
    
    public static JSONObject getMethod(HttpClient client, String endPoint, String authToken)
            throws IOException {
		HttpGet request = new HttpGet(endPoint);
		request.setHeader("Authorization", authToken);
		HttpResponse response = client.execute(request);
		JSONObject responseJson = null;
		StatusLine status = response.getStatusLine();
		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED){
    		JSONObject nonAuthResponse = new JSONObject();
    		LOGGER.error("Request is not authorized , and reason phrase is : {}",EntityUtils.toString(response.getEntity()));
    		nonAuthResponse.put("Not_Auth", true);
    		return nonAuthResponse;
        } else if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CONFLICT) {
        	JSONObject conflict = new JSONObject();
    		LOGGER.error("Request is conflict , and reason phrase is : {}",EntityUtils.toString(response.getEntity()));
        	conflict.put("conflict", true);
        	return conflict;
        } else if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
        	String json = EntityUtils.toString(response.getEntity());
		    LOGGER.error("IOException : {}", json);
		    throw new IOException("StatusCode =" + status.getStatusCode() + " and reason phrase is"
		                    + json);
		} else {
		    responseJson = responseReader(response);
		}
		return responseJson;
	}
    

    public static JSONObject postJSON(HttpClient client, String endPoint, String authToken,
            String postJson) throws ClientProtocolException, IOException {

			HttpPost post = new HttpPost(endPoint);
			post.setHeader("Authorization", authToken);
			StringEntity entity = new StringEntity(postJson,
			ContentType.create("application/json", "UTF-8"));
			entity.setChunked(true);
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			int code = response.getStatusLine().getStatusCode();
			StatusLine status = response.getStatusLine();
			
			LOGGER.debug("Response Code = {}", code);
			
			if(code == HttpStatus.SC_UNAUTHORIZED){
	    		JSONObject nonAuthResponse = new JSONObject();
	    		nonAuthResponse.put("Not_Auth", true);
	    		return nonAuthResponse;
	        }
			else if(code == HttpStatus.SC_NO_CONTENT){
				return null;
			}
			else if(code == HttpStatus.SC_CREATED){
				return responseReader(response);
			}
			else if(code == HttpStatus.SC_OK){
				JSONObject jsonObj = responseReader(response);
				LOGGER.debug("JSONObject = {}", jsonObj.toString());
				return jsonObj;
			}
			else if (code == HttpStatus.SC_CONFLICT) {
				JSONObject conflict = new JSONObject();
				conflict.put("conflict", true);
				return conflict;
			} 
			else{
				throw new IOException("StatusCode =" + status.getStatusCode() + " and reason phrase is "
				+ status.getReasonPhrase());
			}
	}

 
    static JSONObject responseReader(HttpResponse response) throws IOException {
    	JSONObject responseJson = null;
    	try{    		
    		return new JSONObject(EntityUtils.toString(response.getEntity()));
    	}
    	catch(JSONException e){
    		LOGGER.error("JSONException = {}", e.getMessage());
    	}
    	if(response.getStatusLine().getStatusCode() == 200){
    		LOGGER.debug("NOT AUTH Request!");
    		JSONObject json = new JSONObject();
    		json.put("Not_Auth", true);
    		return json;
        }
    	return responseJson;
    }

 

}
