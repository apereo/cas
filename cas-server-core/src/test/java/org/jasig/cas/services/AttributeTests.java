/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class AttributeTests extends TestCase {

    public void testAttributeConstructor() {
        final String id = "test";
        final String name = "name";
        final Attribute attribute = new Attribute(id, name);
        
        assertEquals(id, attribute.getId());
        assertEquals(name, attribute.getName());
    }
    
    public void testAttributeSetters() {
        final String id = "test";
        final String name = "name";
        final Attribute attribute = new Attribute();
        
        attribute.setId(id);
        attribute.setName(name);
        
        assertEquals(id, attribute.getId());
        assertEquals(name, attribute.getName());
        assertEquals(name, attribute.toString());
    }
}
