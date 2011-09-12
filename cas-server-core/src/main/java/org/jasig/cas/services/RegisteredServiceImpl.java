/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;

import javax.persistence.GenerationType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

/**
 * Mutable implementation of a RegisteredService.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
@Entity
public class RegisteredServiceImpl
    implements RegisteredService, Comparable<RegisteredService> {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -5136788302682868276L;

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;
    
    
    @CollectionOfElements(targetElement = String.class, fetch = FetchType.EAGER)
    @JoinTable(name = "rs_attributes")
    @Column(name = "a_name", nullable = false)
    @IndexColumn(name = "a_id")
    private List<String> allowedAttributes = new ArrayList<String>();

    private String description;

    private String serviceId;

    private String name;

    private String theme;

    private boolean allowedToProxy = true;

    private boolean enabled = true;

    private boolean ssoEnabled = true;

    private boolean anonymousAccess = false;
    
    private boolean ignoreAttributes = false;
   
    @Column(name = "evaluation_order", nullable = false)
    private int evaluationOrder;

    public boolean isAnonymousAccess() {
        return this.anonymousAccess;
    }

    public void setAnonymousAccess(final boolean anonymousAccess) {
        this.anonymousAccess = anonymousAccess;
    }

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
        return service != null && PATH_MATCHER.match(this.serviceId.toLowerCase(), service.getId().toLowerCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisteredServiceImpl)) return false;

        RegisteredServiceImpl that = (RegisteredServiceImpl) o;

        if (allowedToProxy != that.allowedToProxy) return false;
        if (anonymousAccess != that.anonymousAccess) return false;
        if (enabled != that.enabled) return false;
        if (evaluationOrder != that.evaluationOrder) return false;
        if (ignoreAttributes != that.ignoreAttributes) return false;
        if (ssoEnabled != that.ssoEnabled) return false;
        if (allowedAttributes != null ? !allowedAttributes.equals(that.allowedAttributes) : that.allowedAttributes != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
        if (theme != null ? !theme.equals(that.theme) : that.theme != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = allowedAttributes != null ? allowedAttributes.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (theme != null ? theme.hashCode() : 0);
        result = 31 * result + (allowedToProxy ? 1 : 0);
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (ssoEnabled ? 1 : 0);
        result = 31 * result + (anonymousAccess ? 1 : 0);
        result = 31 * result + (ignoreAttributes ? 1 : 0);
        result = 31 * result + evaluationOrder;
        return result;
    }

    public void setAllowedAttributes(final List<String> allowedAttributes) {
        if (allowedAttributes == null) {
            this.allowedAttributes = new ArrayList<String>();
        } else {
            this.allowedAttributes = allowedAttributes;
        }
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
    
    public void setEvaluationOrder(final int evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    public int getEvaluationOrder() {
        return this.evaluationOrder;
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
        registeredServiceImpl.setEvaluationOrder(this.evaluationOrder);

        return registeredServiceImpl;
    }
    

    public int compareTo(final RegisteredService other) {
        final int result = this.evaluationOrder - other.getEvaluationOrder();
        if (result == 0) {
            return (int)(this.id - other.getId());
        }
        return result;
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("id", this.id);
        toStringBuilder.append("name", this.name);
        toStringBuilder.append("description", this.description);
        toStringBuilder.append("serviceId", this.serviceId);
        toStringBuilder.append("attributes", this.allowedAttributes.toArray());

        return toStringBuilder.toString();
    }
}
