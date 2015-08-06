/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Return only the collection of allowed attributes out of what's resolved
 * for the principal.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class ReturnAllowedAttributeReleasePolicy extends AbstractAttributeReleasePolicy {

    private static final long serialVersionUID = -5771481877391140569L;
    
    private List<String> allowedAttributes;

    /**
     * Instantiates a new Return allowed attribute release policy.
     */
    public ReturnAllowedAttributeReleasePolicy() {
        this(new ArrayList<String>());
    }

    /**
     * Instantiates a new Return allowed attribute release policy.
     *
     * @param allowedAttributes the allowed attributes
     */
    public ReturnAllowedAttributeReleasePolicy(final List<String> allowedAttributes) {
        this.allowedAttributes = allowedAttributes;
    }

    /**
     * Sets the allowed attributes.
     *
     * @param allowed the allowed attributes.
     */
    public void setAllowedAttributes(final List<String> allowed) {
        this.allowedAttributes = allowed;
    }
    
    /**
     * Gets the allowed attributes.
     *
     * @return the allowed attributes
     */
    public List<String> getAllowedAttributes() {
        return Collections.unmodifiableList(this.allowedAttributes);
    }
    
    @Override
    protected Map<String, Object> getAttributesInternal(final Map<String, Object> resolvedAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>(resolvedAttributes.size());

        for (final String attribute : this.allowedAttributes) {
            final Object value = resolvedAttributes.get(attribute);

            if (value != null) {
                logger.debug("Found attribute [{}] in the list of allowed attributes", attribute);
                attributesToRelease.put(attribute, value);
            }
        }
        return attributesToRelease;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ReturnAllowedAttributeReleasePolicy rhs = (ReturnAllowedAttributeReleasePolicy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.allowedAttributes, rhs.allowedAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .appendSuper(super.hashCode())
                .append(allowedAttributes)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("allowedAttributes", allowedAttributes)
                .toString();
    }
}
