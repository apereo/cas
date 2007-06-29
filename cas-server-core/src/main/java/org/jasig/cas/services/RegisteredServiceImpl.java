/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.persistence.GenerationType;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Mutable implementation of a RegisteredService.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
@Entity
public class RegisteredServiceImpl implements RegisteredService {

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;
    
    @Basic
    private String[] allowedAttributes = new String[0];

    private String description;

    private String serviceId;

    private String name;

    private String theme;

    private boolean allowedToProxy = true;

    private boolean enabled = true;

    private boolean ssoEnabled = true;

    private boolean anonymousAccess = false;

    public boolean isAnonymousAccess() {
        return this.anonymousAccess;
    }

    public void setAnonymousAccess(final boolean anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
    }

    public String[] getAllowedAttributes() {
        /*return this.allowedAttributes != null ? this.allowedAttributes
            : new ArrayList<String>();*/
        return this.allowedAttributes != null ? this.allowedAttributes : new String[0];
    }

    public long getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public String getName() {
        return this.name;
    }

    public String getTheme() {
        return this.theme;
    }

    public boolean isAllowedToProxy() {
        return this.allowedToProxy;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isSsoEnabled() {
        return this.ssoEnabled;
    }

    public boolean matches(final Service service) {
        return PATH_MATCHER.match(this.serviceId, service.getId());
    }

    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof RegisteredService) {
            final RegisteredService r = (RegisteredService) obj;
            return this.getServiceId().equals(r.getServiceId());
        }

        return false;
    }

    public void setAllowedAttributes(final String[] allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    public void setAllowedToProxy(final boolean allowedToProxy) {
        this.allowedToProxy = allowedToProxy;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public void setServiceId(final String id) {
        this.serviceId = id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public void setTheme(final String theme) {
        this.theme = theme;
    }

    public Object clone() throws CloneNotSupportedException {
        final RegisteredServiceImpl registeredServiceImpl = new RegisteredServiceImpl();

        registeredServiceImpl.setAllowedAttributes(this.allowedAttributes);
        registeredServiceImpl.setAllowedToProxy(this.allowedToProxy);
        registeredServiceImpl.setDescription(this.description);
        registeredServiceImpl.setEnabled(this.enabled);
        registeredServiceImpl.setId(this.id);
        registeredServiceImpl.setName(this.name);
        registeredServiceImpl.setServiceId(this.serviceId);
        registeredServiceImpl.setSsoEnabled(this.ssoEnabled);
        registeredServiceImpl.setTheme(this.theme);
        registeredServiceImpl.setAnonymousAccess(this.anonymousAccess);

        return registeredServiceImpl;
    }
}
