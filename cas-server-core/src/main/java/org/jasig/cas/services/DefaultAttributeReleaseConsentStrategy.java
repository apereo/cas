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

import org.jasig.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link AttributeReleaseConsentStrategy}
 * that uses the runtime memory as the persistence storage.
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DefaultAttributeReleaseConsentStrategy implements AttributeReleaseConsentStrategy {
    private static final long serialVersionUID = 4971695890141486653L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Principal, Set<RegisteredService>> consentMapStore = new HashMap<>();

    /**
     * Instantiates a new attribute release consent strategy.
     */
    public DefaultAttributeReleaseConsentStrategy() {
        logger.warn("{} uses runtime memory to keep track of "
                    + "consent options. This is not suitable for a production system.",
                    DefaultAttributeReleaseConsentStrategy.class.getName());
    }
    @Override
    public boolean isAttributeReleaseConsented(final RegisteredService service, final Principal principal) {
        final Set<RegisteredService> services = consentMapStore.get(principal);
        if (services != null && services.contains(service)) {
            logger.debug("Consent is already authorized for {}", service.getName());
            return false;
        }
        return true;
    }

    @Override
    public void setAttributeReleaseConsented(final RegisteredService service, final Principal principal) {
        Assert.notNull(service);
        Assert.notNull(principal);
        Set<RegisteredService> services = consentMapStore.get(principal);
        if (services == null) {
            services = new HashSet<>();
        }
        services.add(service);
        logger.debug("Attribute release consented by {} for {}", principal.getId(),
                service.getName());
        consentMapStore.put(principal, services);
    }
}
