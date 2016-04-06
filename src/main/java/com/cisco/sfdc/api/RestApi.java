package com.cisco.sfdc.api;

import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cisco.sfdc.util.ApiVersion;
import com.cisco.sfdc.util.HttpMethods;


@Service
public class RestApi {
//	public final static String ApiVersion = "35.0";
	public final static String SERVICES = "/services/data/v";
	public final static String QUERY = "/query?q=";
	
    private static Logger logger = LoggerFactory.getLogger(RestApi.class);
    
    public JSONObject salesforceRestCall(String sessionId, String restEndPoint) throws IOException{
    	HttpClient httpclient = new DefaultHttpClient();
    	return HttpMethods.getMethod(httpclient, restEndPoint, "OAuth " + sessionId);
    }
    	
	/*All Sobjects Describe*/
    public JSONObject SobjectsDescribeGlobal(String instanceUrl, String accessToken) throws JSONException, IOException{

    	HttpClient httpclient = new DefaultHttpClient();
    	String endPoint = instanceUrl + "/services/data/+"+ApiVersion.V_CURRENT+"+/sobjects/";
 		JSONObject response = HttpMethods.getMethod(httpclient, endPoint, "OAuth " + accessToken);
 		return response;
    } 
    
    /*One Sobject Describe*/
    public JSONObject SobjectDescribe(String instanceUrl, String accessToken, String sobject) throws JSONException, IOException{
    	
    	HttpClient httpclient = new DefaultHttpClient();
    	String endPoint = instanceUrl + "/services/data/+"+ApiVersion.V_CURRENT+"+/sobjects/"+sobject+"/describe/";
 		JSONObject response = HttpMethods.getMethod(httpclient, endPoint, "OAuth " + accessToken);
 		return response;
    }
    
    /*Auth2.0 Token Refreshing*/
    public JSONObject refreshToken(String clientId , String clientSecret , String instanceUrl , String refreshToken) throws ClientProtocolException, IOException{
		
    	HttpClient httpclient = new DefaultHttpClient();
    	String endpoint = instanceUrl+ "services/oauth2/token";
    	JSONObject post = new JSONObject();
    	post.put("client_id", clientId);
        post.put("client_secret", clientSecret);
        post.put("grant_type", "refresh_token");
        post.put("refresh_token", refreshToken);
        JSONObject response = HttpMethods.postJSON(httpclient, endpoint, null, post.toString());
        logger.debug("RefreshToken request response = {}", response);
        return response;
    }
    
}
