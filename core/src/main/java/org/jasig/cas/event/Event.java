/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.util.Date;

/**
 * 
 * Tagging interface to identify CAS specific events.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public interface Event {
    
    /**
     * Method to return the date/time the event
     * was created.
     * 
     * @return the date the event was created.
     */
    Date getPublishedDate();
}
