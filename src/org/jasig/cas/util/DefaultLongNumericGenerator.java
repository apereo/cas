/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.util;



/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class DefaultLongNumericGenerator implements LongNumericGenerator {
	private static final int MAX_STRING_LENGTH = new Long(Long.MAX_VALUE).toString().length();
	private static final int MIN_STRING_LENGTH = 1;
	private static final boolean DEFAULT_WRAP = true;

	private boolean wrap = DEFAULT_WRAP;
	private long count = 0;
	
	public DefaultLongNumericGenerator() {}
	
	public DefaultLongNumericGenerator(boolean wrap) {
		this.wrap = wrap;
	}
	
	public DefaultLongNumericGenerator(long initialValue, boolean wrap) {
		this.count = initialValue;
		this.wrap  = wrap;
	}
	
	public DefaultLongNumericGenerator(long initialValue) {
		this.count = initialValue;
	}
	
	/**
	 * @see org.jasig.cas.util.LongNumericGenerator#getNextLong()
	 */
	public long getNextLong() {
		return this.getNextValue();
	}

	/**
	 * @see org.jasig.cas.util.NumericGenerator#getNextNumberAsString()
	 */
	public String getNextNumberAsString() {
		return new Long(this.getNextValue()).toString();
	}

	/**
	 * @see org.jasig.cas.util.NumericGenerator#maxLength()
	 */
	public int maxLength() {
		return DefaultLongNumericGenerator.MAX_STRING_LENGTH;
	}

	/**
	 * @see org.jasig.cas.util.NumericGenerator#minLength()
	 */
	public int minLength() {
		return DefaultLongNumericGenerator.MIN_STRING_LENGTH;
	}
	
	protected synchronized long getNextValue() {
		if (!this.wrap && this.count == Long.MAX_VALUE)
			throw new IllegalStateException("Maximum value reached for this number generator.");
		
		return ++this.count;
	}
}
