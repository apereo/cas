package org.jasig.cas.util;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface RandomStringGenerator {
	
	int getMinLength();
	
	int getMaxLength();
	
	String getNewString();
}
