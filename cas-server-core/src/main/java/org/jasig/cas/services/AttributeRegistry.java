/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

/**
 * Maintains a list of attributes. Backing store can be anything from a database
 * to hard-coded values.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface AttributeRegistry {

    /**
     * Retrieves the list of attributes.
     * 
     * @return the list of Attributes.
     */
    List<String> getAttributes();
}
