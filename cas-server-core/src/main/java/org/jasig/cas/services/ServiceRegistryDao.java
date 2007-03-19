/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public interface ServiceRegistryDao {

    void save(RegisteredService registeredService);
    
    boolean delete(RegisteredService registeredService);
    
    List<RegisteredService> load();
}
