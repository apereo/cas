/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;


/**
 * 
 * Exception that is thrown when a ticket retrievel from the registry does not
 * match the expected class.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class InvalidTicketException extends TicketException {
	private static final long serialVersionUID = 6833932903118682012L;

	/**
	 * 
	 */
	public InvalidTicketException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public InvalidTicketException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidTicketException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public InvalidTicketException(Throwable cause)
	{
		super(cause);
	}
}
