/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;

import org.jasig.cas.authentication.principal.Service;


public interface ServicesManager {

    void save(RegisteredService registeredService);
    
    boolean delete(RegisteredService registeredService);
    
    RegisteredService findServiceBy(Service service);
    
    RegisteredService findServiceBy(long id);
    
    Collection<RegisteredService> getAllServices();
    
    boolean matchesExistingService(Service service);
}
