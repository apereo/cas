/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web.support;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.services.Attribute;
import org.jasig.cas.services.MapBackedAttributeRepository;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class AttributePropertyEditorTests extends TestCase {

    private MapBackedAttributeRepository attributeRepository;
    
    private AttributePropertyEditor editor;
    
    private Attribute attr = new Attribute("test", "test");

    protected void setUp() throws Exception {
        this.attributeRepository = new MapBackedAttributeRepository();
        
        final Map<String, Attribute> attributes = new HashMap<String, Attribute>();
        
        attributes.put("test", this.attr);
        
        this.attributeRepository.setAttributes(attributes);
        
        this.editor = new AttributePropertyEditor(this.attributeRepository);
    }
    
    public void testAttributeNotExists() {
        this.editor.setAsText("test2");
        assertNull(this.editor.getValue());
    }

    public void testAttributeExists() {
        this.editor.setAsText("test");
        assertEquals(this.attr, this.editor.getValue());
    }
    
}
