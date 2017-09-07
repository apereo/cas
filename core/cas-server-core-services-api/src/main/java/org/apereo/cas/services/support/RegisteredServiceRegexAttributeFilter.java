package org.apereo.cas.services.support;

import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The regex filter that is responsible to make sure only attributes that match a certain regex pattern
 * registered service are released.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class RegisteredServiceRegexAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 403015306984610128L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceRegexAttributeFilter.class);

    private Pattern pattern;
    private int order;

    /**
     * Instantiates a new Registered service regex attribute filter.
     * Required for serialization.
     */
    protected RegisteredServiceRegexAttributeFilter() {
    }

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
     * <p>
     * Given attribute values may be an extension of {@link Collection}, {@link Map} or an array.
     * <ul>
     * <li>The filtering operation is non-recursive. </li>
     * <li>Multi-valued attributes such as those of type {@link Collection} and
     * {@link Map} are expected to allow casting to {@code Map&lt;String, String&gt;}
     * or {@code Collection&lt;String&gt;}.
     * Values that are of type array are expected to allow casting to {@code String[]}.
     * </li>
     * <li>Multi-valued attributes are always put back into the final released collection of
     * attributes as {@code String[]}.</li>
     * <li>If the final filtered collection is empty, it will not be put into the collection of attributes.</li>
     * </ul>
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>();
        givenAttributes.entrySet()
                .stream()
                .filter(entry -> {
                    final String attributeName = entry.getKey();
                    final Object attributeValue = entry.getValue();
                    LOGGER.debug("Received attribute [{}] with value [{}]", attributeName, attributeValue);
                    return attributeValue != null;
                })
                .forEach(entry -> {
                    final String attributeName = entry.getKey();
                    final Object attributeValue = entry.getValue();

                    if (attributeValue instanceof Collection) {
                        LOGGER.trace("Attribute value [{}] is a collection", attributeValue);
                        final List filteredAttributes = filterAttributes((Collection<String>) attributeValue, attributeName);
                        if (!filteredAttributes.isEmpty()) {
                            attributesToRelease.put(attributeName, filteredAttributes);
                        }
                    } else if (attributeValue.getClass().isArray()) {
                        LOGGER.trace("Attribute value [{}] is an array", attributeValue);
                        final List filteredAttributes = filterAttributes(CollectionUtils.wrapList((String[]) attributeValue), attributeName);
                        if (!filteredAttributes.isEmpty()) {
                            attributesToRelease.put(attributeName, filteredAttributes);
                        }
                    } else if (attributeValue instanceof Map) {
                        LOGGER.trace("Attribute value [{}] is a map", attributeValue);
                        final Map<String, String> filteredAttributes = filterAttributes((Map<String, String>) attributeValue);
                        if (!filteredAttributes.isEmpty()) {
                            attributesToRelease.put(attributeName, filteredAttributes);
                        }
                    } else {
                        LOGGER.trace("Attribute value [{}] is a string", attributeValue);
                        final String attrValue = attributeValue.toString();
                        if (patternMatchesAttributeValue(attrValue)) {
                            logReleasedAttributeEntry(attributeName, attrValue);
                            attributesToRelease.put(attributeName, attrValue);
                        }
                    }
                });

        LOGGER.debug("Received [{}] attributes. Filtered and released [{}]", givenAttributes.size(),
                attributesToRelease.size());
        return attributesToRelease;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    /**
     * Filter map attributes based on the values given.
     *
     * @param valuesToFilter the values to filter
     * @return the map
     */
    private Map<String, String> filterAttributes(final Map<String, String> valuesToFilter) {
        return valuesToFilter.entrySet()
                .stream()
                .filter(entry -> patternMatchesAttributeValue(entry.getValue())).map(entry -> {
                    logReleasedAttributeEntry(entry.getKey(), entry.getValue());
                    return entry;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> valuesToFilter.get(entry.getKey()), (e, f) -> f == null ? e : f));
    }

    /**
     * Filter array attributes.
     *
     * @param valuesToFilter the values to filter
     * @param attributeName  the attribute name
     * @return the string[]
     */
    private List filterAttributes(final Collection<String> valuesToFilter, final String attributeName) {
        return valuesToFilter.stream().filter(this::patternMatchesAttributeValue).map(attributeValue -> {
            logReleasedAttributeEntry(attributeName, attributeValue);
            return attributeValue;
        }).collect(Collectors.toList());
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
     * Logs the released attribute entry.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     */
    private void logReleasedAttributeEntry(final String attributeName, final String attributeValue) {
        LOGGER.debug("The attribute value [{}] for attribute name [{}] matches the pattern [{}]. Releasing attribute...",
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
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("pattern", this.pattern.pattern())
                .toString();
    }
}
