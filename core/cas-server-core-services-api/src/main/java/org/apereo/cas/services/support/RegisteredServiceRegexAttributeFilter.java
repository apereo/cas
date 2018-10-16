package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@Slf4j
@ToString
@Setter
@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"pattern", "order"})
public class RegisteredServiceRegexAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 403015306984610128L;

    private Pattern compiledPattern;

    private String pattern;
    private int order;

    /**
     * Instantiates a new registered service regex attribute filter.
     *
     * @param regex the regex
     */
    public RegisteredServiceRegexAttributeFilter(final String regex) {
        this.compiledPattern = Pattern.compile(regex);
        this.pattern = regex;
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
        val attributesToRelease = new HashMap<String, Object>();
        givenAttributes.entrySet().stream().filter(entry -> {
            val attributeName = entry.getKey();
            val attributeValue = entry.getValue();
            LOGGER.debug("Received attribute [{}] with value [{}]", attributeName, attributeValue);
            return attributeValue != null;
        }).forEach(entry -> {
            val attributeName = entry.getKey();
            val attributeValue = entry.getValue();
            if (attributeValue instanceof Collection) {
                LOGGER.trace("Attribute value [{}] is a collection", attributeValue);
                val filteredAttributes = filterAttributes((Collection<String>) attributeValue, attributeName);
                if (!filteredAttributes.isEmpty()) {
                    attributesToRelease.put(attributeName, filteredAttributes);
                }
            } else if (attributeValue.getClass().isArray()) {
                LOGGER.trace("Attribute value [{}] is an array", attributeValue);
                val filteredAttributes = filterAttributes(CollectionUtils.wrapList((String[]) attributeValue), attributeName);
                if (!filteredAttributes.isEmpty()) {
                    attributesToRelease.put(attributeName, filteredAttributes);
                }
            } else if (attributeValue instanceof Map) {
                LOGGER.trace("Attribute value [{}] is a map", attributeValue);
                val filteredAttributes = filterAttributes((Map<String, String>) attributeValue);
                if (!filteredAttributes.isEmpty()) {
                    attributesToRelease.put(attributeName, filteredAttributes);
                }
            } else {
                LOGGER.trace("Attribute value [{}] is a string", attributeValue);
                val attrValue = attributeValue.toString();
                if (patternMatchesAttributeValue(attrValue)) {
                    logReleasedAttributeEntry(attributeName, attrValue);
                    attributesToRelease.put(attributeName, attrValue);
                }
            }
        });
        LOGGER.debug("Received [{}] attributes. Filtered and released [{}]", givenAttributes.size(), attributesToRelease.size());
        return attributesToRelease;
    }

    /**
     * Filter map attributes based on the values given.
     *
     * @param valuesToFilter the values to filter
     * @return the map
     */
    private Map<String, String> filterAttributes(final Map<String, String> valuesToFilter) {
        return valuesToFilter.entrySet().stream().filter(entry -> patternMatchesAttributeValue(entry.getValue()))
            .peek(entry -> logReleasedAttributeEntry(entry.getKey(), entry.getValue()))
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
        return valuesToFilter.stream().filter(this::patternMatchesAttributeValue)
            .peek(attributeValue -> logReleasedAttributeEntry(attributeName, attributeValue))
            .collect(Collectors.toList());
    }

    /**
     * Determine whether pattern matches attribute value.
     *
     * @param value the value
     * @return true, if successful
     */
    private boolean patternMatchesAttributeValue(final String value) {
        return this.compiledPattern.matcher(value).matches();
    }

    /**
     * Logs the released attribute entry.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     */
    private void logReleasedAttributeEntry(final String attributeName, final String attributeValue) {
        LOGGER.debug("The attribute value [{}] for attribute name [{}] matches the pattern [{}]. Releasing attribute...",
            attributeValue, attributeName, this.compiledPattern.pattern());
    }

}
