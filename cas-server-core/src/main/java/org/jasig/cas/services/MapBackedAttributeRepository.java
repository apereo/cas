/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.util.annotation.NotNull;


public final class MapBackedAttributeRepository implements AttributeRepository {
    
    @NotNull
    private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

    public Attribute getAttribute(final String id) {
        return this.attributes.get(id);
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<Attribute>(this.attributes.values());
    }
    
    public void setAttributes(final Map<String, Attribute> attributes) {
        this.attributes = attributes;
    }
}
