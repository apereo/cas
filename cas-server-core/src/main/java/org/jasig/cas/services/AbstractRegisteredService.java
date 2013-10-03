/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.IndexColumn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.DiscriminatorType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Transient;

/**
 * Base class for mutable, persistable registered services.
 *
 * @author Marvin S. Addison
 * @author Scott Battaglia
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "expression_type", length = 15, discriminatorType = DiscriminatorType.STRING,
                     columnDefinition = "VARCHAR(15) DEFAULT 'ant'")
@Table(name = "RegisteredServiceImpl")
public abstract class AbstractRegisteredService implements RegisteredService, Comparable<RegisteredService>,
        Serializable {

    private static final long serialVersionUID = 7645279151115635245L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @JoinTable(name = "rs_attributes", joinColumns = @JoinColumn(name = "RegisteredServiceImpl_id"))
    @Column(name = "a_name", nullable = false)
    @IndexColumn(name = "a_id")
    private List<String> allowedAttributes = new ArrayList<String>();

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String description;

    /**
     * The unique identifier for this service.
     */
    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    protected String serviceId;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String name;

    @Column(length = 255, updatable = true, insertable = true, nullable = true)
    private String theme;

    private boolean allowedToProxy = false;

    private boolean enabled = true;

    private boolean ssoEnabled = true;

    private boolean anonymousAccess = false;

    private boolean ignoreAttributes = false;

    @Column(name = "evaluation_order", nullable = false)
    private int evaluationOrder;

    /**
     * The attribute filter instance that is responsible for determining the collection of attributes
     * available for release based on this registered service and the filter's policy.
     */
    @Transient
    private RegisteredServiceAttributeFilter attributeFilter = null;

    /**
     * Name of the user attribute that this service expects as the value of the username payload in the
     * validate responses.
     */
    @Column(name = "username_attr", nullable = true, length = 256)
    private String usernameAttribute = null;

    /**
     * The logout type of the service. As front channel SLO is an experimental feature,
     * the default logout type is the back channel one.
     */
    @Transient
    private LogoutType logoutType = LogoutType.BACK_CHANNEL;

    @Lob
    @Column(name = "required_handlers")
    private HashSet<String> requiredHandlers = new HashSet<String>();

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

    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractRegisteredService)) {
            return false;
        }

        final AbstractRegisteredService that = (AbstractRegisteredService) o;

        return new EqualsBuilder().append(this.allowedToProxy, that.allowedToProxy)
                .append(this.anonymousAccess, that.anonymousAccess).append(this.enabled, that.enabled)
                .append(this.evaluationOrder, that.evaluationOrder)
                .append(this.ignoreAttributes, that.ignoreAttributes).append(this.ssoEnabled, that.ssoEnabled)
                .append(this.allowedAttributes, that.allowedAttributes).append(this.description, that.description)
                .append(this.name, that.name).append(this.serviceId, that.serviceId).append(this.theme, that.theme)
                .append(this.usernameAttribute, that.usernameAttribute).append(this.logoutType, that.logoutType)
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder(7, 31).append(this.allowedAttributes).append(this.description)
                .append(this.serviceId).append(this.name).append(this.theme).append(this.enabled)
                .append(this.ssoEnabled).append(this.anonymousAccess).append(this.ignoreAttributes)
                .append(this.evaluationOrder).append(this.usernameAttribute).append(this.logoutType).toHashCode();
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

    public abstract void setServiceId(final String id);

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

    public String getUsernameAttribute() {
        return this.usernameAttribute;
    }

    /**
     * Sets the name of the user attribute to use as the username when providing usernames to this registered service.
     *
     * <p>Note: The username attribute will have no affect on services that are marked for anonymous access.
     *
     * @param username attribute to release for this service that may be one of the following values:
     * <ul>
     *  <li>name of the attribute this service prefers to consume as username</li>.
     *  <li><code>null</code> to enforce default CAS behavior</li>
     * </ul>
     * @see #isAnonymousAccess()
     */
    public void setUsernameAttribute(final String username) {
        if (StringUtils.isBlank(username)) {
            this.usernameAttribute = null;
        } else {
            this.usernameAttribute = username;
        }
    }

    /**
     * Returns the logout type of the service.
     *
     * @return the logout type of the service.
     */
    public final LogoutType getLogoutType() {
        return logoutType;
    }

    /**
     * Set the logout type of the service.
     *
     * @param logoutType the logout type of the service.
     */
    public final void setLogoutType(final LogoutType logoutType) {
        this.logoutType = logoutType;
    }

    public RegisteredService clone() throws CloneNotSupportedException {
        final AbstractRegisteredService clone = newInstance();
        clone.copyFrom(this);
        return clone;
    }

    /**
     * Copies the properties of the source service into this instance.
     *
     * @param source Source service from which to copy properties.
     */
    public void copyFrom(final RegisteredService source) {
        this.setId(source.getId());
        this.setAllowedAttributes(new ArrayList<String>(source.getAllowedAttributes()));
        this.setAllowedToProxy(source.isAllowedToProxy());
        this.setDescription(source.getDescription());
        this.setEnabled(source.isEnabled());
        this.setName(source.getName());
        this.setServiceId(source.getServiceId());
        this.setSsoEnabled(source.isSsoEnabled());
        this.setTheme(source.getTheme());
        this.setAnonymousAccess(source.isAnonymousAccess());
        this.setIgnoreAttributes(source.isIgnoreAttributes());
        this.setEvaluationOrder(source.getEvaluationOrder());
        this.setUsernameAttribute(source.getUsernameAttribute());
        this.setLogoutType(source.getLogoutType());
    }

    /**
     * {@inheritDoc}
     * Compares this instance with the <code>other</code> registered service based on
     * evaluation order, name. The name comparison is case insensitive.
     *
     * @see #getEvaluationOrder()
     */
    @Override
    public int compareTo(final RegisteredService other) {
        return new CompareToBuilder()
                  .append(this.getEvaluationOrder(), other.getEvaluationOrder())
                  .append(this.getName().toLowerCase(), other.getName().toLowerCase())
                  .append(this.getServiceId(), other.getServiceId())
                  .toComparison();
    }

    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("id", this.id);
        toStringBuilder.append("name", this.name);
        toStringBuilder.append("description", this.description);
        toStringBuilder.append("serviceId", this.serviceId);
        toStringBuilder.append("usernameAttribute", this.usernameAttribute);
        toStringBuilder.append("attributes", this.allowedAttributes.toArray());

        return toStringBuilder.toString();
    }

    protected abstract AbstractRegisteredService newInstance();

    public final void setAttributeFilter(final RegisteredServiceAttributeFilter filter) {
        this.attributeFilter = filter;
    }

    public RegisteredServiceAttributeFilter getAttributeFilter() {
        return this.attributeFilter;
    }

    public Set<String> getRequiredHandlers() {
        if (this.requiredHandlers == null) {
            this.requiredHandlers = new HashSet<String>();
        }
        return this.requiredHandlers;
    }

    public void setRequiredHandlers(final Set<String> handlers) {
        getRequiredHandlers().clear();
        if (handlers == null) {
            return;
        }
        for (final String handler : handlers) {
            getRequiredHandlers().add(handler);
        }
    }
}
