package org.jasig.cas.util;


/**
 * Interface to return a new sequential number for each call.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface NumericGenerator {

	String getNextNumberAsString();
	int maxLength();
	int minLength();
}
