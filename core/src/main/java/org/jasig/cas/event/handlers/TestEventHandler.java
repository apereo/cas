/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event.handlers;

import org.jasig.cas.event.EventHandler;
import org.springframework.context.ApplicationEvent;

/**
 * Test EventHandler to demonstrate that event publishing and handling are
 * working. This should not be used in a production environment.
 * <p>
 * Handler simply System.out's the event as a String.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 */
public final class TestEventHandler implements EventHandler {

    /** This method calls the toString on an event and writes it to standard out. */
    public void handleEvent(final ApplicationEvent event) {
        System.out.println(event.toString());
    }

    /**
     * @return always returns true.
     */
    public boolean supports(final ApplicationEvent event) {
        return true;
    }
}
