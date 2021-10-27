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
package org.jasig.cas.services.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default filter that is responsible to make sure only the allowed attributes for a given
 * registered service are released. The allowed attributes are cross checked against
 * the list of principal attributes and those that are a match will be released.
 *
 * If the registered service is set to ignore the attribute release policy, the filter
 * will release all principal attributes.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 * @see RegisteredServiceRegexAttributeFilter
 */
public final class RegisteredServiceDefaultAttributeFilter implements RegisteredServiceAttributeFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> filter(final String principalId, final Map<String, Object> givenAttributes,
            final RegisteredService registeredService) {
        final Map<String, Object> attributes = new HashMap<String, Object>();

        if (registeredService.isIgnoreAttributes()) {
            logger.debug("Service [{}] is set to ignore attribute release policy. Releasing all attributes.",
                    registeredService.getName());
            attributes.putAll(givenAttributes);
        } else {
            for (final String attribute : registeredService.getAllowedAttributes()) {
                final Object value = givenAttributes.get(attribute);

                if (value != null) {
                    logger.debug("Found attribute [{}] in the list of allowed attributes for service [{}]", attribute,
                            registeredService.getName());
                    attributes.put(attribute, value);
                }
            }
        }
        return Collections.unmodifiableMap(attributes);
    }

}
