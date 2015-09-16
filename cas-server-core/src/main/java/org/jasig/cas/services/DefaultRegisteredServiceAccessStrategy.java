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

import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link DefaultRegisteredServiceAccessStrategy}
 * that allows the following rules:
 *
 * <ul>
 *     <li>A service may be disallowed to use CAS for authentication</li>
 *     <li>A service may be disallowed to take part in CAS single sign-on such that
 *     presentation of credentials would always be required.</li>
 *     <li>A service may be prohibited from receiving a service ticket
 *     if the existing principal attributes don't contain the required attributes
 *     that otherwise grant access to the service.</li>
 * </ul>
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAccessStrategy implements RegisteredServiceAccessStrategy {

    private static final long serialVersionUID = 1245279151345635245L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Is the service allowed at all? **/
    private boolean enabled = true;

    /** Is the service allowed to use SSO? **/
    private boolean ssoEnabled = true;

    /**
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    private boolean requireAllAttributes = true;

    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    private Map<String, Set<String>> requiredAttributes = new HashMap<>();

    /**
     * Instantiates a new Default registered service authorization strategy.
     * By default, rules indicate that services are both enabled
     * and can participate in SSO.
     */
    public DefaultRegisteredServiceAccessStrategy() {
        this(true, true);
    }

    /**
     * Instantiates a new Default registered service authorization strategy.
     *
     * @param enabled the enabled
     * @param ssoEnabled the sso enabled
     */
    public DefaultRegisteredServiceAccessStrategy(final boolean enabled, final boolean ssoEnabled) {
        this.enabled = enabled;
        this.ssoEnabled = ssoEnabled;
    }

    public final void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set to enable/authorize this service.
     * @param ssoEnabled true to enable service
     */
    public final void setSsoEnabled(final boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isSsoEnabled() {
        return this.ssoEnabled;
    }

    /**
     * Defines the attribute aggregation when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     * @param requireAllAttributes the require all attributes
     */
    public final void setRequireAllAttributes(final boolean requireAllAttributes) {
        this.requireAllAttributes = requireAllAttributes;
    }

    public final boolean isRequireAllAttributes() {
        return this.requireAllAttributes;
    }

    public Map<String, Set<String>> getRequiredAttributes() {
        return new HashMap<>(this.requiredAttributes);
    }

    /**
     * Defines the required attribute names and values that
     * must be available to the principal before the flow
     * can proceed to the next step. Every attribute in
     * the map can be linked to multiple values.
     *
     * @param requiredAttributes the required attributes
     */
    public final void setRequiredAttributes(final Map<String, Set<String>> requiredAttributes) {
        this.requiredAttributes = requiredAttributes;
    }

    /**
     * {@inheritDoc}
     *
     * Verify presence of service required attributes.
     * <ul>
     *     <li>If no required attributes are specified, authz is granted.</li>
     *     <li>If ALL required attributes must be present, and the principal contains all and there is
     *     at least one attribute value that matches the required, authz is granted.</li>
     *     <li>If ALL required attributes don't have to be present, and there is at least
     *     one principal attribute present whose value matches the required, authz is granted.</li>
     *     <li>Otherwise, access is denied</li>
     * </ul>
     * Note that comparison of principal/required attributes is case-sensitive. Exact matches are required
     * for any individual attribute value.
     */
    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final Map<String, Object> principalAttributes) {
        if (this.requiredAttributes.isEmpty()) {
            logger.debug("No required attributes are specified");
            return true;
        }
        if (principalAttributes.isEmpty()) {
            logger.debug("No principal attributes are found to satisfy attribute requirements");
            return false;
        }

        if (principalAttributes.size() < this.requiredAttributes.size()) {
            logger.debug("The size of the principal attributes that are [{}] does not match requirements, "
                    + "which means the principal is not carrying enough data to grant authorization",
                    principalAttributes);
            return false;
        }

        final Map<String, Set<String>> requiredAttrs = this.getRequiredAttributes();
        logger.debug("These required attributes [{}] are examined against [{}] before service can proceed.",
                requiredAttrs, principalAttributes);

        final Sets.SetView<String> difference = Sets.intersection(requiredAttrs.keySet(), principalAttributes.keySet());
        final Set<String> copy = difference.immutableCopy();

        if (this.requireAllAttributes && copy.size() < this.requiredAttributes.size()) {
            logger.debug("Not all required attributes are available to the principal");
            return false;
        }

        for (final String key : copy) {
            final Set<?> requiredValues = this.requiredAttributes.get(key);
            final Set<?> availableValues;

            final Object objVal = principalAttributes.get(key);
            if (objVal instanceof Collection) {
                final Collection valCol = (Collection) objVal;
                availableValues = Sets.newHashSet(valCol.toArray());
            } else {
                availableValues = Collections.singleton(objVal);
            }

            final Sets.SetView<?> differenceInValues = Sets.intersection(availableValues, requiredValues);
            if (!differenceInValues.isEmpty()) {
                logger.info("Principal is authorized to access the service");
                return true;
            }
        }
        logger.info("Principal is denied access as the required attributes for the registered service are missing");
        return false;
    }

    @Override
    public boolean isServiceAccessAllowedForSso() {
        if (!this.ssoEnabled) {
            logger.trace("Service is not authorized to participate in SSO.");
        }
        return this.ssoEnabled;
    }

    @Override
    public boolean isServiceAccessAllowed() {
        if (!this.enabled) {
            logger.trace("Service is not enabled in service registry.");
        }
        return this.enabled;
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
        final DefaultRegisteredServiceAccessStrategy rhs = (DefaultRegisteredServiceAccessStrategy) obj;
        return new EqualsBuilder()
                .append(this.enabled, rhs.enabled)
                .append(this.ssoEnabled, rhs.ssoEnabled)
                .append(this.requireAllAttributes, rhs.requireAllAttributes)
                .append(this.requiredAttributes, rhs.requiredAttributes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.enabled)
                .append(this.ssoEnabled)
                .append(this.requireAllAttributes)
                .append(this.requiredAttributes)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("enabled", enabled)
                .append("ssoEnabled", ssoEnabled)
                .append("requireAllAttributes", requireAllAttributes)
                .append("requiredAttributes", requiredAttributes)
                .toString();
    }
}
