/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.context.ApplicationEvent;

/**
 * Abstract implementation of the Event interface that defines the method
 * getPublishedDate so that implementing classes do not need to.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractEvent extends ApplicationEvent {

    /** The date the event was published. */
    private final long publishedDate;

    /**
     * Constructor that passes the source object to the super class.
     * 
     * @param o the source object.
     */
    public AbstractEvent(final Object o) {
        super(o);
        this.publishedDate = System.currentTimeMillis();
    }

    /**
     * Method to retrieve the date this event was published. The Date returned
     * by this method is a copy - changing it will not change the Date instances
     * returned by subsequent calls to this method.
     * 
     * @return the Date this event was published.
     */
    public final Date getPublishedDate() {
        return new Date(this.publishedDate);
    }

    public final String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
