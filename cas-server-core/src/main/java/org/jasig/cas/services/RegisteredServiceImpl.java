/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.List;

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
public class RegisteredServiceImpl implements RegisteredService, Cloneable {

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private List<String> allowedAttributes = new ArrayList<String>();

    private String description;

    private String serviceId;

    private String name;

    private String theme;

    private long id = -1;

    private boolean allowedToProxy = true;

    private boolean enabled = true;

    private boolean ssoEnabled = true;

    public List<String> getAllowedAttributes() {
        return this.allowedAttributes != null ? this.allowedAttributes
            : new ArrayList<String>();
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

    public void setAllowedAttributes(final List<String> allowedAttributes) {
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

        return registeredServiceImpl;
    }
}
