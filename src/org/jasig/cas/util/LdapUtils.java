/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import java.util.Iterator;
import java.util.Properties;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public abstract class LdapUtils {

	/**
	 * Utility method to replace the placeholders in the filter with the proper values from the userName.
	 * 
	 * @param filter
	 * @param userName
	 * @return the filtered string populated with the username
	 */
	public static String getFilterWithValues(final String filter, final String userName) {
		Properties properties = new Properties();
		String[] userDomain;
		String newFilter = filter;
		
		properties.setProperty("%u", userName);

		userDomain = userName.split("@");

		properties.setProperty("%U", userDomain[0]);

		if (userDomain.length > 1) {
			properties.setProperty("%d", userDomain[1]);
			
			String[] dcArray = userDomain[1].split("\\.");
				
			for (int i = 0; i < dcArray.length; i++) {
				properties.setProperty("%" + (i + 1), dcArray[dcArray.length - 1 - i]);
			}
		}
		
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String value = properties.getProperty(key, "");
			
			newFilter = newFilter.replaceFirst(key, value); // TODO: should this be replaceAll ????
		}
		
		return newFilter;
	}
}
