/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a list of attributes based on the X.500 Specification (RFC 2256).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class X500AttributeRepository implements AttributeRepository {

    private final List<Attribute> attributes = new ArrayList<Attribute>();

    public X500AttributeRepository() {
        this.attributes.add(new Attribute(0, "cn"));
        this.attributes.add(new Attribute(1, "sn"));
        this.attributes.add(new Attribute(2, "c"));
        this.attributes.add(new Attribute(3, "l"));
        this.attributes.add(new Attribute(4, "st"));
        this.attributes.add(new Attribute(5, "street"));
        this.attributes.add(new Attribute(6, "o"));
        this.attributes.add(new Attribute(7, "ou"));
        this.attributes.add(new Attribute(9, "title"));
        this.attributes.add(new Attribute(10, "description"));
        this.attributes.add(new Attribute(11, "businessCategory"));
        this.attributes.add(new Attribute(12, "postalAddress"));
        this.attributes.add(new Attribute(13, "postalCode"));
        this.attributes.add(new Attribute(14, "postOfficeBox"));
        this.attributes.add(new Attribute(15, "physicalDeliveryOfficeName"));
        this.attributes.add(new Attribute(16, "telephoneNumber"));
        this.attributes.add(new Attribute(17, "telexNumber"));
        this.attributes.add(new Attribute(18, "teletexTerminalIdentifier"));
        this.attributes.add(new Attribute(19, "facsimileTelephoneNumber"));
        this.attributes.add(new Attribute(20, "title"));
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    // XXX: optimize this with a map
    public Attribute getAttribute(final long id) {
        for (final Attribute a : this.attributes) {
            if (a.getId() == id) {
                return a;
            }
        }
        return null;
    }
}
