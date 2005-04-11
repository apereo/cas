/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.util.Date;

/**
 * Abstract implementation of the Event interface that defines
 * the method getPublishedDate so that implementing classes
 * do not need to.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public abstract class AbstractEvent implements Event {
    
    private final Date publishedDate;
    
    public AbstractEvent() {
        this.publishedDate = new Date();
    }

    public final Date getPublishedDate() {
        return this.publishedDate;
    }
}
