/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.util.Date;


public abstract class AbstractEvent implements Event {
    
    private final Date publishedDate;
    
    public AbstractEvent() {
        this.publishedDate = new Date();
    }

    public final Date getPublishedDate() {
        return this.publishedDate;
    }
}
