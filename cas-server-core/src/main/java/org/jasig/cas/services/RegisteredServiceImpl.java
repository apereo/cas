/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;
import java.util.regex.Pattern;

import org.jasig.cas.authentication.principal.Service;

/**
 * Mutable implementation of a RegisteredService.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class RegisteredServiceImpl implements RegisteredService {

    private List<String> allowedAttributes;

    private String description;

    private String serviceId;

    private String name;

    private String theme;
    
    private long id;

    private boolean allowedToProxy = true;

    private boolean enabled = true;

    private boolean ssoEnabled = true;
    
    private Pattern idPattern;
    
    private boolean matchExactly;

    public List<String> getAllowedAttributes() {
        return this.allowedAttributes;
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
        if (service == null && this.serviceId == null) {
            return true;
        }
        
        if (service == null) {
            return false;
        }

        if (this.idPattern != null) {
            return this.idPattern.matcher(service.getId()).matches();
        }

        return service.getId().equals(this.serviceId);
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
        compilePattern();
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
    
    public boolean isMatchExactly() {
        return this.matchExactly;
    }
    
    public void setMatchExactly(final boolean matchExactly) {
        this.matchExactly = matchExactly;
        compilePattern();
    }
    
    private void compilePattern() {
        if (!this.matchExactly && this.serviceId != null) {
            this.idPattern = Pattern.compile(this.serviceId);
        } else {
            this.idPattern = null;
        }
    }
}
