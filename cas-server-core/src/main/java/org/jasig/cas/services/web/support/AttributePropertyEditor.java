/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web.support;

import java.beans.PropertyEditorSupport;

import org.jasig.cas.services.AttributeRepository;
import org.jasig.cas.util.annotation.NotNull;

/**
 * Convert a String to an Attribute
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class AttributePropertyEditor extends PropertyEditorSupport {
    
    @NotNull
    private final AttributeRepository attributeRepository;
    
    public AttributePropertyEditor(final AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public void setAsText(final String text) throws IllegalArgumentException {
        setValue(this.attributeRepository.getAttribute(text));
    }
}
