/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.flow.Event;

/**
 * Holder for both Model and Event in the CAS work flow.
 * Note that these are entirely distinct. This class merely holds
 * both to make it possible for a action to return both model
 * and event in a single return value.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ModelAndEvent {

    private Event event;

    private Map model = new HashMap(1);

    public ModelAndEvent(final Event event) {
        this.event = event;
    }

    public ModelAndEvent(final Event event, final Map model) {
        this(event);
        this.model = model;
    }

    public ModelAndEvent(final Event event, final String attribute,
        final Object value) {
        this(event);
        this.model.put(attribute, value);
    }

    public final Map getModel() {
        return this.model;
    }

    public final Event getEvent() {
        return this.event;
    }
}
