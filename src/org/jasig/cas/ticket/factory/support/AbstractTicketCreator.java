/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.factory.support;

import org.jasig.cas.ticket.factory.TicketCreator;
import org.jasig.cas.util.UniqueTicketIdGenerator;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public abstract class AbstractTicketCreator implements TicketCreator {
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    /**
     * @return Returns the uniqueTicketIdGenerator.
     */
    public UniqueTicketIdGenerator getUniqueTicketIdGenerator() {
        return this.uniqueTicketIdGenerator;
    }
    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(
            UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }
}
