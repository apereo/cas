/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.event;

import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.event.handlers.TestEventHandler;
import org.jasig.cas.mock.MockApplicationEvent;
import org.jasig.cas.mock.MockEventHandler;
import org.springframework.context.ApplicationEvent;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class EventListenerTests extends TestCase {
    private EventListener eventListener;

    protected void setUp() throws Exception {
        this.eventListener = new EventListener();
        
        this.eventListener.setEventHandlers(new EventHandler[] {new TestEventHandler()});
        
        this.eventListener.afterPropertiesSet();
    }
    
    public void testHandlEvent() {
        AuthenticationEvent e = new AuthenticationEvent(new UsernamePasswordCredentials(), true, AuthenticationHandler.class);
        
        this.eventListener.onApplicationEvent(e);
    }
    
    public void testNoEventToHandle() {
        ApplicationEvent e = new MockApplicationEvent("Test");
        
        this.eventListener.onApplicationEvent(e);
    }
    
    public void testNotHandlEvent() {
        AuthenticationEvent e = new AuthenticationEvent(new UsernamePasswordCredentials(), true, AuthenticationHandler.class);
        
        
        this.eventListener.setEventHandlers(new EventHandler[] {new MockEventHandler()});
        
        this.eventListener.onApplicationEvent(e);
    }
    
    public void testAfterPropertiesSet() {
       this.eventListener.setEventHandlers(null);
       
       try {
           this.eventListener.afterPropertiesSet();
           fail("Exception expected.");
       } catch (Exception e) {
           return;
       }
    }

}
