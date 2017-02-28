/*******************************************************************************
 * Copyright (c) 2017, Wavity Inc. and/or its affiliates. All rights reserved.
 *******************************************************************************/

package org.jasig.cas.util;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class EncryptationAwarePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer{

	@Override
	protected String convertPropertyValue(String originalValue) {
		return decrypt(originalValue);
	}


    /**
     * @param ciphertext
     * @return
     */
    private String decrypt(final String ciphertext) 
	{
    	//TODO decryption logic here
	    return ciphertext;
	}
}