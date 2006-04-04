/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public interface TicketState {

    int getCountOfUses();

    long getLastTimeUsed();

    long getPreviousTimeUsed();

    long getCreationTime();
}
