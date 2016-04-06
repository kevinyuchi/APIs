package com.cisco.sfdc.helper;

public class SalesforceQueryHelper {
	public static String readSobjectTypeFromSOQL(String soql){
		int fromIndex = soql.lastIndexOf("from");
		String tableNameString = soql.substring(fromIndex+5);
		if(tableNameString.contains(" ")){
			 return tableNameString.substring(0 , tableNameString.indexOf(" "));
		}
		return tableNameString;
	}
}
