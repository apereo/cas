/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

/**
 * Interface that enables for pluggable unique ticket Ids strategies.
 * 
 * @author Scott Battaglia
 * @version $Id: UniqueTicketIdGenerator.java,v 1.1 2005/02/15 05:06:38
 * sbattaglia Exp $
 */
public interface UniqueTicketIdGenerator {

    /**
     * Return a new unique ticket id beginning with the prefix.
     * 
     * @param prefix The prefix we want attached to the ticket.
     * @return the unique ticket id
     */
    public String getNewTicketId(String prefix);
}