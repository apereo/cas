/*
 * @(#)StringResponseReadCallback.java $version 2015. 1. 23.
 *
 * Copyright 2014 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jasig.cas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author ruaa
 * @since 2015. 1. 23.
 */
public class StringResponseReadCallback implements HttpTemplate.ResponseReadCallback<String> {

	/* (non-Javadoc)
	 * @see com.nhnent.webnote.util.HttpsUtil.ResponseReadCallback#read(java.io.BufferedReader)
	 */
	@Override
	public String read(InputStream inputStream) {
		String inputLine;
		StringBuffer sbResponse = new StringBuffer();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		
		try{
			while((inputLine = br.readLine()) != null) {
				sbResponse.append(inputLine);
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return sbResponse.toString();
	}

}
