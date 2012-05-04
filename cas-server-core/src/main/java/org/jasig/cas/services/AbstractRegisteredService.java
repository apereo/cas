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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for mutable, persistable registered services.
 *
 * @author Marvin S. Addison
 * @author Scott Battaglia
 */
@Entity
@Inheritance
@DiscriminatorColumn(
        name = "expression_type",
        length = 15,
        discriminatorType = DiscriminatorType.STRING,
        columnDefinition = "VARCHAR(15) DEFAULT 'ant'")
@Table(name = "RegisteredServiceImpl")
public abstract class AbstractRegisteredService
        implements RegisteredService, Comparable<RegisteredService>, Serializable {

    /** Serialization version marker  */
    private static final long serialVersionUID = 7645279151115635245L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @JoinTable(name = "rs_attributes", joinColumns = @JoinColumn(name = "RegisteredServiceImpl_id"))
    @Column(name = "a_name", nullable = false)
    @IndexColumn(name = "a_id")
    private List<String> allowedAttributes = new ArrayList<String>();

    private String description;

    protected String serviceId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRegisteredService)) return false;

        final AbstractRegisteredService that = (AbstractRegisteredService) o;

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

    public Object clone() throws CloneNotSupportedException {
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
    }

    public int compareTo(final RegisteredService other) {
        final int result = this.evaluationOrder - other.getEvaluationOrder();
        if (result == 0) {
            return (int) (this.id - other.getId());
        }
        return result;
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("id", this.id);
        toStringBuilder.append("name", this.name);
        toStringBuilder.append("description", this.description);
        toStringBuilder.append("serviceId", this.serviceId);
        toStringBuilder.append("attributes", this.allowedAttributes.toArray());

        return toStringBuilder.toString();
    }

    protected abstract AbstractRegisteredService newInstance();
}