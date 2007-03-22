/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1 TODO javadoc
 */
@Entity
public class Attribute {

    @Id
    private long id;

    private String name;

    public Attribute() {
        // nothing to do
    }

    public Attribute(final long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return this.id;
    }
    
    public String toString() {
        return this.name;
    }
}
