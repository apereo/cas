package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredServiceRegexAttributeFilter implements RegisteredServiceAttributeFilter {

    private static final long serialVersionUID = 403015306984610128L;

    @JsonIgnore
    private Pattern compiledPattern;

    private String pattern;
    private int order;

    /**
     * Instantiates a new registered service regex attribute filter.
     *
     * @param regex the regex
     */
    @JsonCreator
    public RegisteredServiceRegexAttributeFilter(@JsonProperty("pattern") final String regex) {
        this.compiledPattern = RegexUtils.createPattern(regex);
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
    public Map<String, List<Object>> filter(final Map<String, List<Object>> givenAttributes) {
        val attributesToRelease = new HashMap<String, List<Object>>();
        givenAttributes.entrySet()
            .stream()
            .filter(entry -> {
                val attributeName = entry.getKey();
                val attributeValue = entry.getValue();
                LOGGER.debug("Received attribute [{}] with value [{}]", attributeName, attributeValue);
                return attributeValue != null;
            })
            .forEach(entry -> {
                val attributeName = entry.getKey();
                val attributeValue = entry.getValue();
                LOGGER.trace("Attribute value [{}] is a collection", attributeValue);
                val filteredAttributes = filterAttributes(attributeValue, attributeName);
                if (!filteredAttributes.isEmpty()) {
                    attributesToRelease.put(attributeName, filteredAttributes);
                }
            });
        LOGGER.debug("Received [{}] attributes. Filtered and released [{}]", givenAttributes.size(), attributesToRelease.size());
        return attributesToRelease;
    }

    /**
     * Filter array attributes.
     *
     * @param valuesToFilter the values to filter
     * @param attributeName  the attribute name
     * @return the string[]
     */
    private List filterAttributes(final List<Object> valuesToFilter, final String attributeName) {
        return valuesToFilter
            .stream()
            .filter(this::patternMatchesAttributeValue)
            .peek(attributeValue -> logReleasedAttributeEntry(attributeName, attributeValue))
            .collect(Collectors.toList());
    }

    /**
     * Determine whether pattern matches attribute value.
     *
     * @param value the value
     * @return true, if successful
     */
    private boolean patternMatchesAttributeValue(final Object value) {
        val matcher = value.toString();
        LOGGER.trace("Compiling a pattern matcher for [{}]", matcher);
        return this.compiledPattern.matcher(matcher).matches();
    }

    /**
     * Logs the released attribute entry.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     */
    private void logReleasedAttributeEntry(final String attributeName, final Object attributeValue) {
        LOGGER.debug("The attribute value [{}] for attribute name [{}] matches the pattern [{}]. Releasing attribute...",
            attributeValue, attributeName, this.compiledPattern.pattern());
    }

}
