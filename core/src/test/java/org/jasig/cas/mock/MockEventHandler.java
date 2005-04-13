/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.mock;

import org.jasig.cas.event.EventHandler;
import org.springframework.context.ApplicationEvent;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class MockEventHandler implements EventHandler {

    public void handleEvent(ApplicationEvent event) {
        // this does not have to do anything
    }

    public boolean supports(ApplicationEvent event) {
        return false;
    }

}
