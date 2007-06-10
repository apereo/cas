/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Represents an attribute for a user that may be returned with a service in the payload.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
@Entity
public class Attribute {

    /** The unique identifier for this id. */
    @Id
    private String id;

    /** The name of the attribute. */
    private String name;

    /**
     * Default constructor.
     */
    public Attribute() {
        // nothing to do
    }

    /**
     * Constructor that takes the id and name.
     * @param id the unique identifier for this attribute.
     * @param name the name of the attribute.
     */
    public Attribute(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public String toString() {
        return this.name;
    }
}
