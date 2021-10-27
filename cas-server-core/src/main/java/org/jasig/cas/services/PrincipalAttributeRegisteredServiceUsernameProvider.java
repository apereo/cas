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
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Determines the username for this registered service based on a principal attribute.
 * If the attribute is not found, default principal id is returned.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProvider implements RegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -3546719400741715137L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @NotNull
    private final String usernameAttribute;

    /**
     * Private constructor to get around serialization issues.
     */
    private PrincipalAttributeRegisteredServiceUsernameProvider() {
        this.usernameAttribute = null;
    }

    /**
     * Instantiates a new default registered service username provider.
     *
     * @param usernameAttribute the username attribute
     */
    public PrincipalAttributeRegisteredServiceUsernameProvider(@NotNull final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }
    
    public String getUsernameAttribute() {
        return this.usernameAttribute;
    }

    @Override
    public String resolveUsername(final Principal principal, final Service service) {
        String principalId = principal.getId();
        final Map<String, Object> originalPrincipalAttributes = principal.getAttributes();
        final Map<String, Object> attributes = getPrincipalAttributes(principal, service);

        if (attributes.containsKey(this.usernameAttribute)) {
            principalId = attributes.get(this.usernameAttribute).toString();
        } else if (originalPrincipalAttributes.containsKey(this.usernameAttribute)) {
            logger.warn("The selected username attribute [{}] was retrieved as a direct "
                       + "principal attributes and not through the attribute release policy for service [{}]. "
                       + "CAS is unable to detect new attribute values for [{}] after authentication unless the attribute "
                       + "is explicitly authorized for release via the service attribute release policy.", 
                    this.usernameAttribute, service, this.usernameAttribute);
            principalId = originalPrincipalAttributes.get(this.usernameAttribute).toString();
        } else {
            logger.warn("Principal [{}] does not have an attribute [{}] among attributes [{}] so CAS cannot "
                            + "provide the user attribute the service expects. "
                            + "CAS will instead return the default principal id [{}]. Ensure the attribute selected as the username "
                            + "is allowed to be released by the service attribute release policy.",
                    principalId,
                    this.usernameAttribute,
                    attributes,
                    principalId);
        }

        logger.debug("Principal id to return for [{}] is [{}]. The default principal id is [{}].",
                service.getId(), principalId, principal.getId());
        return principalId;
    }
    
    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("usernameAttribute", this.usernameAttribute);
        return toStringBuilder.toString();
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
        final PrincipalAttributeRegisteredServiceUsernameProvider rhs =
                (PrincipalAttributeRegisteredServiceUsernameProvider) obj;
        return new EqualsBuilder()
                .append(this.usernameAttribute, rhs.usernameAttribute)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(usernameAttribute)
                .toHashCode();
    }

    /**
     * Gets principal attributes. Will attempt to locate the principal
     * attribute repository from the context if one is defined to use
     * that instance to locate attributes. If none is available,
     * will use the default principal attributes.
     *
     * @param p       the principal
     * @param service the service
     * @return the principal attributes
     */
    protected Map<String, Object> getPrincipalAttributes(final Principal p, final Service service) {
        final ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        if (context != null) {
            logger.debug("Located application context to locate the service registry entry");
            final ReloadableServicesManager servicesManager = context.getBean(ReloadableServicesManager.class);
            if (servicesManager != null) {
                final RegisteredService registeredService = servicesManager.findServiceBy(service);

                if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                    logger.debug("Located service {} in the registry. Attempting to resolve attributes for {}",
                            service.getId(), p.getId());

                    if (registeredService.getAttributeReleasePolicy() == null) {
                        logger.debug("No attribute release policy is defined for {}. Returning default principal attributes",
                                service.getId());
                        return p.getAttributes();
                    }
                    return registeredService.getAttributeReleasePolicy().getAttributes(p);
                }
            }

            logger.debug("Could not locate service {} in the registry.", service.getId());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        logger.warn("No application context could be detected. Returning default principal attributes");
        return p.getAttributes();
    }
}
