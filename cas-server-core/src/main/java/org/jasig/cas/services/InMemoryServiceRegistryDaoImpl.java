/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public final class InMemoryServiceRegistryDaoImpl implements ServiceRegistryDao {
    
    @NotNull
    private List<RegisteredService> registeredServices = new ArrayList<RegisteredService>();
    
    public boolean delete(RegisteredService registeredService) {
        return this.registeredServices.remove(registeredService);
    }

    public RegisteredService findServiceById(final long id) {
        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() == id) {
                return r;
            }
        }
        
        return null;
    }

    public List<RegisteredService> load() {
        return this.registeredServices;
    }

    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == -1) {
            ((AbstractRegisteredService) registeredService).setId(findHighestId()+1);
        }

        this.registeredServices.remove(registeredService);
        this.registeredServices.add(registeredService);
        
        return registeredService;
    }

    public void setRegisteredServices(final List<RegisteredService> registeredServices) {
        this.registeredServices = registeredServices;
    }

    /**
     * This isn't super-fast but I don't expect thousands of services.
     *
     * @return
     */
    private long findHighestId() {
        long id = 0;

        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() > id) {
                id = r.getId();
            }
        }

        return id;
    }
}
