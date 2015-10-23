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
package org.jasig.cas.services.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.services.AttributeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * The regex filter that is responsible to make sure only attributes that match a certain regex pattern
 * registered service are released.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public final class RegisteredServiceRegexAttributeFilter implements AttributeFilter {
    private static final long serialVersionUID = 403015306984610128L;
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @NotNull
    private Pattern pattern;

    /**
     * Instantiates a new Registered service regex attribute filter.
     * Required for serialization.
     */
    protected RegisteredServiceRegexAttributeFilter() {}

    /**
     * Instantiates a new registered service regex attribute filter.
     *
     * @param regex the regex
     */
    public RegisteredServiceRegexAttributeFilter(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Gets the pattern.
     *
     * @return the pattern
     */
    public Pattern getPattern() {
        return this.pattern;
    }
    
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
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>();
        for (final Map.Entry<String, Object> entry: givenAttributes.entrySet()) {
            final String attributeName = entry.getKey();
            final Object attributeValue = entry.getValue();

            logger.debug("Received attribute [{}] with value [{}]", attributeName, attributeValue);
            if (attributeValue != null) {
                if (attributeValue instanceof Collection) {
                    logger.trace("Attribute value {} is a collection", attributeValue);
                    final String[] filteredAttributes = filterArrayAttributes(
                            ((Collection<String>) attributeValue).toArray(new String[] {}), attributeName);
                    if (filteredAttributes.length > 0) {
                        attributesToRelease.put(attributeName, Arrays.asList(filteredAttributes));
                    }
                } else if (attributeValue.getClass().isArray()) {
                    logger.trace("Attribute value {} is an array", attributeValue);
                    final String[] filteredAttributes = filterArrayAttributes((String[]) attributeValue, attributeName);
                    if (filteredAttributes.length > 0) {
                        attributesToRelease.put(attributeName, Arrays.asList(filteredAttributes));
                    }
                } else if (attributeValue instanceof Map) {
                    logger.trace("Attribute value {} is a map", attributeValue);
                    final Map<String, String> filteredAttributes = filterMapAttributes((Map<String, String>) attributeValue);
                    if (filteredAttributes.size() > 0) {
                        attributesToRelease.put(attributeName, filteredAttributes);
                    }
                } else {
                    logger.trace("Attribute value {} is a string", attributeValue);
                    final String attrValue = attributeValue.toString();
                    if (patternMatchesAttributeValue(attrValue)) {
                        logReleasedAttributeEntry(attributeName, attrValue);
                        attributesToRelease.put(attributeName, attrValue);
                    }
                }
            }
        }

        logger.debug("Received {} attributes. Filtered and released {}", givenAttributes.size(),
                attributesToRelease.size());
        return attributesToRelease;
    }

    /**
     * Filter map attributes based on the values given.
     *
     * @param valuesToFilter the values to filter
     * @return the map
     */
    private Map<String, String> filterMapAttributes(final Map<String, String> valuesToFilter) {
        final Map<String, String> attributesToFilter = new HashMap<>(valuesToFilter.size());
        for (final Map.Entry<String, String> entry: valuesToFilter.entrySet()) {
            final String attributeName = entry.getKey();
            final String attributeValue = entry.getValue();
            if (patternMatchesAttributeValue(attributeValue)) {
                logReleasedAttributeEntry(attributeName, attributeValue);
                attributesToFilter.put(attributeName, valuesToFilter.get(attributeName));
            }
        }
        return attributesToFilter;
    }

    /**
     * Determine whether pattern matches attribute value.
     *
     * @param value the value
     * @return true, if successful
     */
    private boolean patternMatchesAttributeValue(final String value) {
        return this.pattern.matcher(value).matches();
    }

    /**
     * Filter array attributes.
     *
     * @param valuesToFilter the values to filter
     * @param attributeName the attribute name
     * @return the string[]
     */
    private String[] filterArrayAttributes(final String[] valuesToFilter, final String attributeName) {
        final List<String> vector = new ArrayList<>(valuesToFilter.length);
        for (final String attributeValue : valuesToFilter) {
            if (patternMatchesAttributeValue(attributeValue)) {
                logReleasedAttributeEntry(attributeName, attributeValue);
                vector.add(attributeValue);
            }
        }
        return vector.toArray(new String[] {});
    }

    /**
     * Logs the released attribute entry.
     *
     * @param attributeName the attribute name
     * @param attributeValue the attribute value
     */
    private void logReleasedAttributeEntry(final String attributeName, final String attributeValue) {
        logger.debug("The attribute value [{}] for attribute name {} matches the pattern {}. Releasing attribute...",
                attributeValue, attributeName, this.pattern.pattern());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 83).append(this.pattern).toHashCode();
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
        final RegisteredServiceRegexAttributeFilter rhs = (RegisteredServiceRegexAttributeFilter) obj;
        return new EqualsBuilder().append(this.pattern.pattern(), rhs.getPattern().pattern()).isEquals();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pattern", this.pattern.pattern())
                .toString();
    }
}
