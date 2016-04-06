package com.cisco.sfdc.common;

public class OauthTokens {
	private String authToken;
	private String baseUrl;
	
	public void setTokens(String authToken, String baseUrl){
		this.authToken = authToken;
		this.baseUrl = baseUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void print() {
		// TODO Auto-generated method stub
		
	}
	
}
