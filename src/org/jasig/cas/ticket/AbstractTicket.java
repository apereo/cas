/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;


/**
 * Abstract implementation of a ticket that handles all ticket state for policies. Also incorporates properties common
 * among all tickets.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractTicket implements Ticket {

    final private ExpirationPolicy expirationPolicy;
    private long lastTimeUsed;
    private int count;
    final private Principal principal;
    final private String id;

    public AbstractTicket(final String id, final Principal person, final ExpirationPolicy expirationPolicy) {
        if (expirationPolicy == null || id == null || person == null)
            throw new IllegalArgumentException("id, person and expirationPolicy are required parameters.");

        this.principal = person;
        this.id = id;
        this.lastTimeUsed = System.currentTimeMillis();

        this.expirationPolicy = expirationPolicy;
    }

    /**
     * @see org.jasig.cas.ticket.Ticket#getPrincipal()
     */
    public Principal getPrincipal() {
        return this.principal;
    }

    /**
     * @see org.jasig.cas.ticket.Ticket#getId()
     */
    public String getId() {
        return id;
    }

    public int getCountOfUses() {
        return count;
    }

    public long getLastUsedTime() {
        return lastTimeUsed;
    }
    
    public void incrementCount()
    {
    	count++;
    }
    
    public void updateLastUse()
    {
    	this.lastTimeUsed = System.currentTimeMillis();
    }

    /**
     * @see org.jasig.cas.ticket.Ticket#isExpired()
     */
    public boolean isExpired() {
        return expirationPolicy.isExpired(this);
    }
    
    public boolean equals(Object o) {
    	return EqualsBuilder.reflectionEquals(this, o);
    }
    
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}