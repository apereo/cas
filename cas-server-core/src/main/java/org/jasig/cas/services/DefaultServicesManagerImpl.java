/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.principal.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.github.inspektr.audit.annotation.Audit;

/**
 * Default implementation of the {@link ServicesManager} interface. If there are
 * no services registered with the server, it considers the ServicecsManager
 * disabled and will not prevent any service from using CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class DefaultServicesManagerImpl implements ReloadableServicesManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Instance of ServiceRegistryDao. */
    @NotNull
    private ServiceRegistryDao serviceRegistryDao;

    /** Map to store all services. */
    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<Long, RegisteredService>();

    /** Default service to return if none have been registered. */
    private RegisteredService disabledRegisteredService;

	/** Array that holds all sorted services by the evaluation order and is auto-updated when services are added/removed **/
	private RegisteredService[]							sortedServices	= new RegisteredService[0];
    
	public DefaultServicesManagerImpl(final ServiceRegistryDao serviceRegistryDao) {
        this(serviceRegistryDao, new ArrayList<String>());
    }
    
    /**
     * Constructs an instance of the {@link DefaultServicesManagerImpl} where the default RegisteredService
     * can include a set of default attributes to use if no services are defined in the registry.
     * 
     * @param serviceRegistryDao the Service Registry Dao.
     * @param defaultAttributes the list of default attributes to use.
     */
    public DefaultServicesManagerImpl(final ServiceRegistryDao serviceRegistryDao, final List<String> defaultAttributes) {
        this.serviceRegistryDao = serviceRegistryDao;
        this.disabledRegisteredService = constructDefaultRegisteredService(defaultAttributes);
        
        load();
    }

    @Transactional(readOnly = false)
    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER", resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService r = findServiceBy(id);
        if (r == null) {
            return null;
        }
        
        this.serviceRegistryDao.delete(r);
        this.services.remove(id);
        
		sortRegisteredServices();

        return r;
    }

    /**
     * Note, if the repository is empty, this implementation will return a default service to grant all access.
     * <p>
     * This preserves default CAS behavior.
     */
    public RegisteredService findServiceBy(final Service service) {
		if (this.sortedServices.length == 0)
            return this.disabledRegisteredService;

		for (final RegisteredService r : this.sortedServices) {
			if (r.matches(service))
                return r;

        }

        return null;
    }


    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);
        
        try {
            return r == null ? null : (RegisteredService) r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }
    
	/**
	 * Sorts the registered services by the evaluation order. The {@link Comparator} for each service
	 * is called when services are collected into the <code>Set</code> and determines the strictness of a service pattern.
	 * Each registered service is designed to be a {@link Comparable}
	 * 
	 * @see RegisteredServiceImpl#compareTo()
	 */
	protected void sortRegisteredServices() {

		if (log.isInfoEnabled())
			log.info("Sorting " + this.services.size() + " registered services by evaluation order");

		final Set<RegisteredService> set = new HashSet<RegisteredService>(this.services.values());
		this.sortedServices = set.toArray(new RegisteredServiceImpl[0]);
		Arrays.sort(this.sortedServices);

	}

	protected final List<RegisteredService> getSortedServies() {
		return Arrays.asList(this.sortedServices);
	}

	/**
	 * {@inheritDoc}
	 *  
	 * @see #getSortedServies()
	 * @return The sorted list of all services, by the evaluation order.
	 */
	public final Collection<RegisteredService> getAllServices() {
		return getSortedServies();
    }

    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Transactional(readOnly = false)
    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER", resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    public synchronized void save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);

		sortRegisteredServices();
    }
    
    public void reload() {
        log.info("Reloading registered services.");
        load();
    }
    
    private void load() {
        final ConcurrentHashMap<Long, RegisteredService> localServices = new ConcurrentHashMap<Long, RegisteredService>();
                
        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            log.debug("Adding registered service " + r.getServiceId());
            localServices.put(r.getId(), r);
        }
        
        this.services = localServices;

		sortRegisteredServices();

        log.info(String.format("Loaded %s services.", this.services.size()));
    }
    
    private RegisteredService constructDefaultRegisteredService(final List<String> attributes) {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setAllowedToProxy(true);
        r.setAnonymousAccess(false);
        r.setEnabled(true);
        r.setSsoEnabled(true);
        r.setAllowedAttributes(attributes);
        
        if (attributes == null || attributes.isEmpty()) {
            r.setIgnoreAttributes(true);
        }

        return r;
    }
}
