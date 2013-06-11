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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RegisteredServiceRegexAttributeFilter(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @NotNull
    private Pattern pattern;

    /**
     * {@inheritDoc}
     *
     * Given attribute values may be an extension of {@link Collection}, {@link Map} or an array.
     * <ul>
     * <li>The filtering operation is non-recursive. </li>
     * <li>Multi-valued attributes such as those of type {@link Collection} and
     * {@link Map} are expected to allow casting to <code>Map&lt;String, String&gt;</code>
     * or <code>Collection&lt;String&gt;</code>.
     * Values that are of type array are expected to allow casting to <code>String[]</code>.
     * </li>
     * <li>Multi-valued attributes are always put back into the final released collection of
     * attributes as <code>String[]</code>.</li>
     * <li>If the final filtered collection is empty, it will not be put into the collection of attributes.</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> filter(final String principalId, final Map<String, Object> givenAttributes,
            final RegisteredService registeredService) {
        final Map<String, Object> attributesToRelease = new HashMap<String, Object>();
        for (final String attributeName : givenAttributes.keySet()) {
            final Object attributeValue = givenAttributes.get(attributeName);

            logger.debug("Received attribute [{}] with value [{}]", attributeName, attributeValue);
            if (attributeValue != null) {
                if (attributeValue instanceof Collection) {
                    final String[] filteredAttributes = filterArrayAttributes(
                            ((Collection<String>) attributeValue).toArray(new String[] {}), attributeName);
                    if (filteredAttributes.length > 0) {
                        attributesToRelease.put(attributeName, filteredAttributes);
                    }
                } else if (attributeValue.getClass().isArray()) {
                    final String[] filteredAttributes = filterArrayAttributes((String[]) attributeValue, attributeName);
                    if (filteredAttributes.length > 0) {
                        attributesToRelease.put(attributeName, filteredAttributes);
                    }
                } else if (attributeValue instanceof Map) {
                    final Map<String, String> filteredAttributes = filterMapAttributes((Map<String, String>) attributeValue);
                    if (filteredAttributes.size() > 0) {
                        attributesToRelease.put(attributeName, filteredAttributes);
                    }
                } else if (patternMatchesAttributeValue(attributeValue.toString())) {
                    logReleasedAttributeEntry(attributeName, attributeValue.toString());
                    attributesToRelease.put(attributeName, attributeValue);
                }
            }
        }

        logger.debug("Received {} attributes. Filtered and released {}", givenAttributes.size(),
                attributesToRelease.size());
        return attributesToRelease;
    }

    private Map<String, String> filterMapAttributes(final Map<String, String> valuesToFilter) {
        final Map<String, String> attributesToFilter = new HashMap<String, String>(valuesToFilter.size());
        for (final String attributeName : valuesToFilter.keySet()) {
            final String attributeValue = valuesToFilter.get(attributeName);
            if (patternMatchesAttributeValue(attributeValue)) {
                logReleasedAttributeEntry(attributeName, attributeValue);
                attributesToFilter.put(attributeName, valuesToFilter.get(attributeName));
            }
        }
        return attributesToFilter;
    }

    private boolean patternMatchesAttributeValue(final String value) {
        return this.pattern.matcher(value).matches();
    }

    private String[] filterArrayAttributes(final String[] valuesToFilter, final String attributeName) {
        final Vector<String> vector = new Vector<String>(valuesToFilter.length);
        for (final String attributeValue : valuesToFilter) {
            if (patternMatchesAttributeValue(attributeValue)) {
                logReleasedAttributeEntry(attributeName, attributeValue);
                vector.add(attributeValue);
            }
        }
        return vector.toArray(new String[] {});
    }

    private void logReleasedAttributeEntry(final String attributeName, final String attributeValue) {
        logger.debug("The attribute value [{}] for attribute name {} matches the pattern {}. Releasing attribute...",
                attributeValue, attributeName, this.pattern.pattern());
    }
}
