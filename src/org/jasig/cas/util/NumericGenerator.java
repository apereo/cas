package org.jasig.cas.util;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface NumericGenerator {

	String getNextNumberAsString();
	int maxLength();
	int minLength();
}
