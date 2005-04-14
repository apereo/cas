/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import org.springframework.context.ApplicationEvent;

/**
 * Interface of classes that know how to handle a specific event.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public interface EventHandler {
    
    /**
     * Method to handle any processing of the event that
     * is needed.
     * 
     * @param event the event to handle.
     */
    void handleEvent(ApplicationEvent event);
    
    /**
     * Method to check if this handler will be able to process
     * the event.
     * 
     * @param event the event we want to check if we support.
     * @return true if the event is supported, false otherwise.
     */
    boolean supports(ApplicationEvent event);
}
