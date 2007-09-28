/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.event;

import java.util.List;

import org.jasig.cas.util.annotation.NotEmpty;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Implementation of an ApplicationListener that listens specifically for events
 * within the CAS domain. Upon receiving an event that it can handle, the
 * listener attempts to delegate the event to one or more event handlers to
 * process the event (i.e. a Log4JLoggedInEventHandler and a
 * DbLoggedInEventHandler).
 * <p>
 * The following properties are required to be set:
 * </p>
 * <ul>
 * <li>eventHandlers - the list of event handlers that can process events.</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class EventListener implements ApplicationListener {

    /** The array of event handlers. */
    @NotEmpty
    private List<EventHandler> eventHandlers;

    public void onApplicationEvent(final ApplicationEvent applicationEvent) {
        if (!AbstractEvent.class.isAssignableFrom(applicationEvent.getClass())) {
            return;
        }

        for (final EventHandler eventHandler : this.eventHandlers) {
            if (eventHandler.supports(applicationEvent)) {
                eventHandler.handleEvent(applicationEvent);
            }
        }
    }

    /**
     * Method to set the Event Handlers to process events.
     * 
     * @param eventHandlers the handlers.
     */
    public void setEventHandlers(final List<EventHandler> eventHandlers) {
        this.eventHandlers = eventHandlers;
    }
}
