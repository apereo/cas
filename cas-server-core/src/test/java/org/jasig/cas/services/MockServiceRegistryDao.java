/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.List;

public class MockServiceRegistryDao implements ServiceRegistryDao {
    
    private boolean loaded;
    
    public MockServiceRegistryDao() {
        this(false);
    }
    
    public MockServiceRegistryDao(final boolean loaded) {
        this.loaded = loaded;
    }

    public boolean delete(RegisteredService registeredService) {
        return false;
    }

    public RegisteredService findServiceById(long id) {
        return new RegisteredServiceImpl();
    }

    public List<RegisteredService> load() {
        final List<RegisteredService> l = new ArrayList<RegisteredService>();
        
        if (this.loaded) {
            final RegisteredServiceImpl impl = new RegisteredServiceImpl();
            impl.setId(2500);
            impl.setServiceId("id");
            
            l.add(impl);
        }
        return l;
    }

    public RegisteredService save(RegisteredService registeredService) {
        return registeredService;
    }
}
