/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import org.springframework.context.ApplicationEvent;

/**
 * Interface of classes that know how to handle a specific event. The concept of
 * handling an event usually implies that the handler will perform some form of
 * logging such as using Log4j or writing the information to a database.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>This is a published and supported CAS Server 3 API.</p>
 */
public interface EventHandler {

    /**
     * Method to handle any processing of the event that is needed.
     * 
     * @param event the event to handle.
     */
    void handleEvent(ApplicationEvent event);

    /**
     * Method to check if this handler will be able to process the event.
     * 
     * @param event the event we want to check if we support.
     * @return true if the event is supported, false otherwise.
     */
    boolean supports(ApplicationEvent event);
}
