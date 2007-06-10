/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;

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
    
    Authentication getAuthentication();
}
