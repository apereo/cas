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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The regex filter that is responsible to make sure only attributes that match a certain regex pattern
 * registered service are released. 
 * 
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class RegisteredServiceRegexAttributeFilter implements RegisteredServiceAttributeFilter {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private Pattern pattern;

    private boolean serviceRegistryIgnoreAttributesEnabled = false;
    
    @Override
    public Map<String, Object> filter(final String principalId, final Map<String, Object> givenAttributes, final RegisteredService registeredService) {
        if (this.serviceRegistryIgnoreAttributesEnabled && registeredService.isIgnoreAttributes()) {
            log.debug("Service [{}] is set to ignore attribute release policy. Releasing all attributes.", registeredService);
            return givenAttributes;
        }
        
        final Map<String, Object> attributes = new HashMap<String, Object>();
        for (final String attribute : givenAttributes.keySet()) {
            final Object value = givenAttributes.get(attribute);

            if (value != null) {
                if (value.getClass().isAssignableFrom(List.class)) {
                    for (final Object listValue : (List<?>) value) {
                        verifyAttributeValueAgainstPattern(attribute, listValue, attributes);
                    }
                } else {
                    verifyAttributeValueAgainstPattern(attribute, value, attributes);
                }
            }
        }
        return attributes;
    }

    /**
     * Regex pattern to match against the attribute value
     */
    public void setPattern(final String regex) {
        this.pattern = Pattern.compile(regex);
    }
    
    /**
     * When set to true, this attribute filter will honor {@link RegisteredService#isIgnoreAttributes()}
     * and will ignore the regex pattern, releasing all given attributes.
     */
    public void setServiceRegistryIgnoreAttributesEnabled(final boolean flag) {
        this.serviceRegistryIgnoreAttributesEnabled = flag;
    }
    
    private void verifyAttributeValueAgainstPattern(final String attribute, final Object value, final Map<String, Object> attributes) {
        final Matcher matcher = this.pattern.matcher(value.toString());
        if (matcher.find()) {
            log.debug("Found attribute [{}] that matches the specified pattern [{}]", attribute, this.pattern.pattern());
            attributes.put(attribute, value);
        }
    }
}
