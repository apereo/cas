/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class MapBackedAttributeRepositoryTests extends TestCase {

    private MapBackedAttributeRepository attributeRepository;
    
    private Attribute attr = new Attribute("test", "test");
    
    private Attribute attr2 = new Attribute("test2", "test2");
    
    protected void setUp() throws Exception {
        this.attributeRepository = new MapBackedAttributeRepository();
        
        final Map<String, Attribute> attributes = new HashMap<String, Attribute>();
        
        attributes.put("test", this.attr);
        attributes.put("test2", this.attr2);
        
        this.attributeRepository.setAttributesMap(attributes);
    }
    
    public void testGetAttribute() {
        assertEquals(this.attr, this.attributeRepository.getAttribute("test"));
    }
    
    public void testGetAttributeNotExists() {
        assertNull(this.attributeRepository.getAttribute("test4"));
    }

    public void testGetAttributes() {
        final List<Attribute> attributes = this.attributeRepository.getAttributes();
        
        assertEquals(2, attributes.size());
        assertTrue(attributes.contains(this.attr));
        assertTrue(attributes.contains(this.attr2));
    }
}
