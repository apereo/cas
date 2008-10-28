/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;

import javax.persistence.GenerationType;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
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

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5136788302682868276L;

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;
    
    
    @CollectionOfElements
    @JoinTable(name = "rs_attributes")
    @Column(name = "a_name", nullable = false)
    @IndexColumn(name = "a_id")
    private String[] allowedAttributes = new String[0];

    private String description;

    private String serviceId;

    private String name;

    private String theme;

    private boolean allowedToProxy = true;

    private boolean enabled = true;

    private boolean ssoEnabled = true;

    private boolean anonymousAccess = false;
    
    private boolean ignoreAttributes = false;

    public boolean isAnonymousAccess() {
        return this.anonymousAccess;
    }

    public void setAnonymousAccess(final boolean anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
    }

    public String[] getAllowedAttributes() {
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
        return service != null && PATH_MATCHER.match(this.serviceId.toLowerCase(), service.getId().toLowerCase());
    }
    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.allowedAttributes);
        result = prime * result + (this.allowedToProxy ? 1231 : 1237);
        result = prime * result + (this.anonymousAccess ? 1231 : 1237);
        result = prime * result
            + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + (this.enabled ? 1231 : 1237);
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result
            + ((this.serviceId == null) ? 0 : this.serviceId.hashCode());
        result = prime * result + (this.ssoEnabled ? 1231 : 1237);
        result = prime * result + ((this.theme == null) ? 0 : this.theme.hashCode());
        return result;
    }

    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RegisteredServiceImpl))
            return false;
        final RegisteredServiceImpl other = (RegisteredServiceImpl) obj;
        if (!Arrays.equals(this.allowedAttributes, other.allowedAttributes))
            return false;
        if (this.allowedToProxy != other.allowedToProxy)
            return false;
        if (this.anonymousAccess != other.anonymousAccess)
            return false;
        if (this.description == null) {
            if (other.description != null)
                return false;
        } else if (!this.description.equals(other.description))
            return false;
        if (this.enabled != other.enabled)
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        if (this.serviceId == null) {
            if (other.serviceId != null)
                return false;
        } else if (!this.serviceId.equals(other.serviceId))
            return false;
        if (this.ssoEnabled != other.ssoEnabled)
            return false;
        if (this.theme == null) {
            if (other.theme != null)
                return false;
        } else if (!this.theme.equals(other.theme))
            return false;
        return true;
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
    
    public boolean isIgnoreAttributes() {
        return this.ignoreAttributes;
    }
    
    public void setIgnoreAttributes(final boolean ignoreAttributes) {
        this.ignoreAttributes = ignoreAttributes;
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
        registeredServiceImpl.setIgnoreAttributes(this.ignoreAttributes);

        return registeredServiceImpl;
    }
}
