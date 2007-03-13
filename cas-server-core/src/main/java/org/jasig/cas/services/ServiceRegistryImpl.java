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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.annotation.Transactional;
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

    private static final String SQL_INSERT = "Insert into cas_service(id, allowedToProxy, description, enabled, name, serviceUrl, ssoEnabled, theme) Values(?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE = "Update cas_service set allowToProxy = ?, description = ?, enabled = ?, name = ?, serviceUrl = ?, ssoEnabled = ?, theme = ? where id = ?";

    private static final String SQL_INSERT_ATTRIBUTE = "Insert into cas_service_attributes (cas_service_id, attribute_id) values (?, (Select id from attributes where name = ?));";

    private static final String SQL_DELETE_ATTRIBUTES = "Delete From cas_service_attributes Where cas_service_id = ?";

    private static final String SQL_DELETE = "Delete From cas_service Where id = ?";

    private static final String SQL_SELECT = "Select id, allowedToProxy, description, enabled, name, serviceUrl, ssoEnabled, theme from cas_service";

    private static final String SQL_SELECT_ENABLED = "Select pref_value from cas_preferences where pref_key = \"cas.serviceregistry.enabled\"";

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
        Assert.notNull(this.dataFieldMaxValueIncrementer,
            "dataFieldMaxValueIncrementer cannot be null.");

        final RegisteredServiceMappingSqlQuery q = new RegisteredServiceMappingSqlQuery(
            getDataSource(), SQL_SELECT);

        this.services.addAll(q.execute());

        try {
            final String enabledValue = getSimpleJdbcTemplate().queryForObject(
                SQL_SELECT_ENABLED, String.class);
    
            this.enabled = Boolean.parseBoolean(enabledValue);
        } catch (final DataAccessException e) {
            this.enabled = false;
        }
    }

    @Transactional(readOnly=false)
    public synchronized void addService(final RegisteredService service) {
        if (this.services.contains(service)) {
            throw new IllegalArgumentException("service ["
                + service.getServiceId() + "] already exists in Registry.");
        }

        ((RegisteredServiceImpl) service)
            .setId(this.dataFieldMaxValueIncrementer.nextLongValue());

        getSimpleJdbcTemplate().update(SQL_INSERT, new Long(service.getId()),
            Boolean.toString(service.isAllowedToProxy()),
            service.getDescription(), Boolean.toString(service.isEnabled()),
            service.getName(), service.getServiceId(),
            Boolean.toString(service.isSsoEnabled()), service.getTheme());

        if (service.getAllowedAttributes() != null) {
            for (final String id : service.getAllowedAttributes()) {
                getSimpleJdbcTemplate().update(SQL_INSERT_ATTRIBUTE,
                    new Long(service.getId()), id);
            }
        }
        
        this.services.add(service);
    }

    @Transactional(readOnly=false)
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

    @Transactional(readOnly=false)
    public synchronized void updateService(final RegisteredService service) {

        getSimpleJdbcTemplate().update(SQL_UPDATE,
            Boolean.toString(service.isAllowedToProxy()),
            service.getDescription(), Boolean.toString(service.isEnabled()),
            service.getName(), service.getServiceId(),
            Boolean.toString(service.isSsoEnabled()), service.getTheme(),
            new Long(service.getId()));

        getSimpleJdbcTemplate().update(SQL_DELETE_ATTRIBUTES,
            new Long(service.getId()));

        for (final String id : service.getAllowedAttributes()) {
            getSimpleJdbcTemplate().update(SQL_INSERT_ATTRIBUTE,
                new Long(service.getId()), id);
        }

    }

    /**
     * Do not call this from a Dependency Injection Framework. It depends on a
     * DataSource being set up.
     * 
     * @see org.jasig.cas.services.ServiceRegistry#setEnabled(boolean)
     */
    @Transactional(readOnly=false)
    public synchronized void setEnabled(final boolean enabled) {
        this.enabled = enabled;

        if (getSimpleJdbcTemplate()
            .queryForInt(
                "Select count(*) from cas_preferences where pref_key = \"cas.serviceregistry.enabled\"") > 0) {
            getSimpleJdbcTemplate()
                .update(
                    "Update cas_preferences Set pref_value = ? where pref_key = \"cas.serviceregistry.enabled\"",
                    Boolean.toString(enabled));
        } else {
            getSimpleJdbcTemplate()
                .update(
                    "Insert into cas_preferences (pref_key, pref_value) values (\"cas.serviceregistry.enabled\", ?)",
                    Boolean.toString(enabled));
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setDataFieldMaxValueIncrementer(
        final DataFieldMaxValueIncrementer dataFieldMaxValueIncrementer) {
        this.dataFieldMaxValueIncrementer = dataFieldMaxValueIncrementer;
    }
    
    protected class RegisteredServiceMappingSqlQuery extends MappingSqlQuery {

        protected RegisteredServiceMappingSqlQuery(final DataSource dataSource,
            final String sql) {
            super(dataSource, sql);
        }

        protected Object mapRow(final ResultSet rs, final int rownum)
            throws SQLException {

            final List<String> attributes = getJdbcTemplate()
                .queryForList(
                    "Select attributes.name from attributes, cas_service_attributes where attributes.id = cas_service_attributes.attribute_id and cas_service_attributes.cas_service_id = ?",
                    new Object[] {new Long(rs.getLong("id"))}, String.class);

            final RegisteredServiceImpl registeredService = new RegisteredServiceImpl();
            registeredService.setAllowedToProxy(Boolean.parseBoolean(rs
                .getString("allowedToProxy")));
            registeredService.setDescription(rs.getString("description"));
            registeredService.setEnabled(Boolean.parseBoolean(rs
                .getString("enabled")));
            registeredService.setId(rs.getLong("id"));
            registeredService.setName(rs.getString("name"));
            registeredService.setServiceId(rs.getString("serviceUrl"));
            registeredService.setSsoEnabled(Boolean.parseBoolean(rs
                .getString("ssoEnabled")));
            registeredService.setTheme(rs.getString("theme"));
            registeredService.setAllowedAttributes(attributes);

            return registeredService;
        }
    }
}
