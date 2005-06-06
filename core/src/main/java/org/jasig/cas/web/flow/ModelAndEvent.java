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
 * Holder for both Model and Event in the CAS work flow. Note that these are
 * entirely distinct. This class merely holds both to make it possible for a
 * action to return both model and event in a single return value.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ModelAndEvent {

    /** The triggered event. */
    private Event event;

    /** The map containing all the elements to pass to the next action. */
    private Map model = new HashMap(1);

    /**
     * Constructs a new ModelAndEvent with a blank Model and the supplied event.
     * 
     * @param event the event that was triggered.
     */
    public ModelAndEvent(final Event event) {
        this.event = event;
    }

    /**
     * Constructs a new ModelAndEvent with the supplied event and map.
     * 
     * @param event the event that was triggered.
     * @param model the map containing all the elements for the Model.
     */
    public ModelAndEvent(final Event event, final Map model) {
        this(event);
        this.model = model;
    }

    /**
     * Constructs a new ModelAndEent with the supplied event and populating a
     * map with the supplied attribute/value pair.
     * 
     * @param event the event that was triggered.
     * @param attribute the single attribute for our model.
     * @param value the single value associated with the attribue.
     */
    public ModelAndEvent(final Event event, final String attribute,
        final Object value) {
        this(event);
        this.model.put(attribute, value);
    }

    /**
     * Return the model.
     * 
     * @return the model.
     */
    public final Map getModel() {
        return this.model;
    }

    /**
     * Return the triggered event.
     * 
     * @return the triggered event.
     */
    public final Event getEvent() {
        return this.event;
    }
}
