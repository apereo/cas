/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.util.Assert;

/**
 * Implementation of the ServiceRegistry and ServiceRegistryManager interfaces.
 * TODO: javadoc
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ServiceRegistryImpl extends SimpleJdbcDaoSupport implements
    ServiceRegistry, ServiceRegistryManager, InitializingBean {

    private static final String SQL_INSERT = "Insert into cas_service(id, allowedToProxy, description, enabled, name, serviceUrl, ssoEnabled, theme) Values(?, ?, ?, ?, ? ?, ?, ?)";
    
    private static final String SQL_UPDATE = "Update cas_service set allowToProxy = ?, description = ?, enabled = ?, name = ?, serviceUrl = ?, ssoEnabled = ?, theme = ? where id = ?";
    
    private static final String SQL_INSERT_ATTRIBUTE = "Insert into cas_service_attributes (cas_service_id, attribute_id) values (?, (Select id from cas_service_attributes where name = ?));";
    
    private static final String SQL_DELETE_ATTRIBUTES = "Delete From cas_service_attributes Where cas_service_id = ?";
    
    private static final String SQL_DELETE = "Delete From cas_service Where id = ?";
    
    /**
     * The list of Registered Services. Utilizes a CopyOnWriteArrayList so that
     * Iterators are threadsafe.
     */
    private List<RegisteredService> services = new CopyOnWriteArrayList<RegisteredService>();

    /**
     * The default empty registered service to return if the registry is
     * disabled.
     */
    private static final RegisteredService EMPTY_REGISTERED_SERVICE = new RegisteredServiceImpl();

    /** Whether the registry is enabled or not. */
    private boolean enabled = true;

    private DataFieldMaxValueIncrementer dataFieldMaxValueIncrementer;

    public RegisteredService findServiceBy(final Service service) {
        if (!this.enabled) {
            return EMPTY_REGISTERED_SERVICE;
        }

        for (final RegisteredService registeredService : this.services) {
            if (registeredService.matches(service)
                && registeredService.isEnabled()) {
                return registeredService;
            }
        }

        return null;
    }

    public RegisteredService findServiceBy(final long id) {
        for (final RegisteredService registeredService : this.services) {
            if (registeredService.getId() == id) {
                return registeredService;
            }
        }

        return null;
    }

    public List<RegisteredService> getAllServices() {
        return Collections.unmodifiableList(this.services);
    }

    public boolean matchesExistingService(final Service service) {
        return !this.enabled || findServiceBy(service) != null;
    }

    protected void initDao() throws Exception {
        Assert.notNull(this.dataFieldMaxValueIncrementer, "dataFieldMaxValueIncrementer cannot be null.");
        
        
        // TODO load from database
    }

    public synchronized void addService(final RegisteredService service) {
        if (this.services.contains(service)) {
            throw new IllegalArgumentException("service ["
                + service.getServiceId() + "] already exists in Registry.");
        }

        ((RegisteredServiceImpl) service)
            .setId(this.dataFieldMaxValueIncrementer.nextLongValue());
        this.services.add(service);
        
        
        getSimpleJdbcTemplate().update(SQL_INSERT, new Long(service.getId()), Boolean.toString(service.isAllowedToProxy()), service.getDescription(), Boolean.toString(service.isEnabled()), service.getName(), service.getServiceId(), Boolean.toString(service.isSsoEnabled()), service.getTheme());

        for (final String id : service.getAllowedAttributes()) {
            getSimpleJdbcTemplate().update(SQL_INSERT_ATTRIBUTE, new Long(service.getId()), id);
        }
    }

    public synchronized boolean deleteService(final long id) {
        if (id == -1) {
            return false;
        }

        for (final RegisteredService r : this.services) {
            if (r.getId() == id) {
                getSimpleJdbcTemplate().update(SQL_DELETE, new Long(id));
                return this.services.remove(r);
            }
        }

        return false;
    }

    public synchronized void updateService(final RegisteredService service) {

        getSimpleJdbcTemplate().update(SQL_UPDATE, Boolean.toString(service.isAllowedToProxy()), service.getDescription(), Boolean.toString(service.isEnabled()), service.getName(), service.getServiceId(), Boolean.toString(service.isSsoEnabled()), service.getTheme(), new Long(service.getId()));
        
        getSimpleJdbcTemplate().update(SQL_DELETE_ATTRIBUTES, new Long(service.getId()));

        for (final String id : service.getAllowedAttributes()) {
            getSimpleJdbcTemplate().update(SQL_INSERT_ATTRIBUTE, new Long(service.getId()), id);
        }

    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;

        if (getSimpleJdbcTemplate()
            .queryForInt(
                "Select count(*) from cas_preferences where key = \"cas.serviceregistry.enabled\"") > 0) {
            getSimpleJdbcTemplate()
                .update(
                    "Update cas_preferences Set value = ? where key = \"cas.serviceregistry.enabled\"",
                    Boolean.toString(enabled));
        } else {
            getSimpleJdbcTemplate()
                .update(
                    "Insert into cas_preferences (key, value) values (\"cas.serviceregistry.enabled\", ?)",
                    Boolean.toString(enabled));
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setBootstrapService(final String serviceId) {
        final RegisteredServiceImpl registeredService = new RegisteredServiceImpl();
        registeredService.setServiceId(serviceId);
        registeredService.setId(-1);
        registeredService
            .setDescription("Default bootstrap service so we can log into the management application.");
        registeredService.setEnabled(true);
        registeredService.setName("Bookstrap Services Management Application");
        registeredService.setSsoEnabled(true);

        this.services.add(registeredService);
    }
    
    protected class RegisteredServiceMappingSqlQuery extends MappingSqlQuery {

        protected RegisteredServiceMappingSqlQuery(final DataSource dataSource, final String sql) {
            super(dataSource, sql);
        }
        
        protected Object mapRow(final ResultSet rs, final int rownum) throws SQLException {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
}
