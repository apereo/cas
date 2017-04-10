package org.apereo.cas.services.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A filtering policy that selectively applies patterns to attributes mapped in the config.
 * If an attribute is mapped, it's only allowed to be released if it matches the linked pattern.
 * If an attribute is not mapped, it may optionally be excluded from the released set of attributes.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceMappedRegexAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 852145306984610128L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceMappedRegexAttributeFilter.class);
    private Map<String, String> patterns;
    private boolean excludeUnmappedAttributes;
    private boolean completeMatch;
    private int order;

    public RegisteredServiceMappedRegexAttributeFilter() {
    }

    public RegisteredServiceMappedRegexAttributeFilter(final Map<String, String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>();
        givenAttributes.entrySet()
                .stream()
                .filter(entry -> {
                    final String attributeName = entry.getKey();
                    final Object attributeValue = entry.getValue();
                    LOGGER.debug("Received attribute [{}] with value(s) [{}]", attributeName, attributeValue);
                    return attributeValue != null;
                })
                .forEach(entry -> {
                    final String attributeName = entry.getKey();
                    if (patterns.containsKey(attributeName)) {
                        final Set<Object> attributeValues = CollectionUtils.toCollection(entry.getValue());
                        final Pattern pattern = RegexUtils.createPattern(patterns.get(attributeName));
                        LOGGER.debug("Found attribute [{}] in the pattern definitions. Processing pattern [{}]", attributeName, pattern.pattern());
                        final List<Object> filteredValues = filterAttributeValuesByPattern(attributeValues, pattern);
                        LOGGER.debug("Filtered attribute values for [{}] are [{}]", attributeName, filteredValues);

                        if (filteredValues.isEmpty()) {
                            LOGGER.debug("Attribute [{}] has no values remaining and shall be excluded", attributeName);
                        } else {
                            attributesToRelease.put(attributeName, filteredValues);
                        }
                    } else {
                        LOGGER.debug("Found attribute [{}] that is not defined in pattern definitions", attributeName);
                        if (excludeUnmappedAttributes) {
                            LOGGER.debug("Excluding attribute [{}] given unmatched attributes are to be excluded", attributeName);
                        } else {
                            LOGGER.debug("Added unmatched attribute [{}] with value(s) [{}]", entry.getKey(), entry.getValue());
                            attributesToRelease.put(entry.getKey(), entry.getValue());
                        }
                    }
                });
        LOGGER.debug("Received [{}] attributes. Filtered and released [{}]", givenAttributes.size(), attributesToRelease.size());
        return attributesToRelease;
    }

    /**
     * Filter attribute values by pattern and return the list.
     *
     * @param attributeValues the attribute values
     * @param pattern         the pattern
     * @return the list
     */
    protected List<Object> filterAttributeValuesByPattern(final Set<Object> attributeValues, final Pattern pattern) {
        return attributeValues.stream()
                .filter(v -> {
                    final Matcher matcher = pattern.matcher(v.toString());
                    LOGGER.debug("Matching attribute value [{}] against pattern [{}]", v, pattern.pattern());
                    if (completeMatch) {
                        return matcher.matches();
                    }
                    return matcher.find();
                })
                .collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    public Map<String, String> getPatterns() {
        return patterns;
    }

    public void setPatterns(final Map<String, String> patterns) {
        this.patterns = patterns;
    }

    public boolean isExcludeUnmappedAttributes() {
        return excludeUnmappedAttributes;
    }

    public boolean isCompleteMatch() {
        return completeMatch;
    }

    public void setCompleteMatch(final boolean completeMatch) {
        this.completeMatch = completeMatch;
    }

    public void setExcludeUnmappedAttributes(final boolean excludeUnmappedAttributes) {
        this.excludeUnmappedAttributes = excludeUnmappedAttributes;
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
        final RegisteredServiceMappedRegexAttributeFilter rhs = (RegisteredServiceMappedRegexAttributeFilter) obj;
        return new EqualsBuilder()
                .append(this.patterns, rhs.patterns)
                .append(this.excludeUnmappedAttributes, rhs.excludeUnmappedAttributes)
                .append(this.completeMatch, rhs.completeMatch)
                .append(this.order, rhs.order)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(patterns)
                .append(excludeUnmappedAttributes)
                .append(completeMatch)
                .append(order)
                .toHashCode();
    }
}
