package org.apereo.cas.services.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

/**
 * This is {@link RegisteredServiceMutantRegexAttributeFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@NoArgsConstructor
public class RegisteredServiceMutantRegexAttributeFilter extends RegisteredServiceMappedRegexAttributeFilter {

    private static final long serialVersionUID = 543145306984660628L;

    @Override
    public Map<String, Object> filter(final Map<String, Object> givenAttributes) {
        final Map<String, Object> attributesToRelease = new HashMap<>();
        givenAttributes.entrySet().stream().filter(filterProvidedGivenAttributes()).forEach(entry -> {
            final String attributeName = entry.getKey();
            if (getPatterns().containsKey(attributeName)) {
                final Set<Object> attributeValues = CollectionUtils.toCollection(entry.getValue());
                LOGGER.debug("Found attribute [{}] in pattern definitions with value(s) [{}]", attributeName, attributeValues);
                final Collection<Pair<Pattern, String>> patterns = createPatternsAndReturnValue(attributeName);
                final List<Object> finalValues = patterns.stream().map(patternDefn -> {
                    final Pattern pattern = patternDefn.getLeft();
                    LOGGER.debug("Found attribute [{}] in the pattern definitions. Processing pattern [{}]", attributeName, pattern.pattern());
                    final List<Object> filteredValues = filterAndMapAttributeValuesByPattern(attributeValues, pattern, patternDefn.getValue());
                    LOGGER.debug("Filtered attribute values for [{}] are [{}]", attributeName, filteredValues);
                    return filteredValues;
                }).flatMap(Collection::stream).collect(Collectors.toList());
                if (finalValues.isEmpty()) {
                    LOGGER.debug("Attribute [{}] has no values remaining and shall be excluded", attributeName);
                } else {
                    collectAttributeWithFilteredValues(attributesToRelease, attributeName, finalValues);
                }
            } else {
                handleUnmappedAttribute(attributesToRelease, entry.getKey(), entry.getValue());
            }
        });
        LOGGER.debug("Received [{}] attributes. Filtered and released [{}]", givenAttributes.size(), attributesToRelease.size());
        return attributesToRelease;
    }

    private Collection<Pair<Pattern, String>> createPatternsAndReturnValue(final String attributeName) {
        final Object patternDef = getPatterns().get(attributeName);
        final List<Object> patternAndReturnVal = new ArrayList<>(CollectionUtils.toCollection(patternDef));
        return patternAndReturnVal.stream().map(p -> {
            final int index = p.toString().indexOf("->");
            if (index != -1) {
                final String patternStr = p.toString().substring(0, index).trim();
                final Pattern pattern = RegexUtils.createPattern(patternStr, isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0);
                final String returnValue = p.toString().substring(index + 2).trim();
                LOGGER.debug("Created attribute filter pattern [{}] with the mapped return value template [{}]", patternStr, returnValue);
                return Pair.of(pattern, returnValue);
            }
            final Pattern pattern = RegexUtils.createPattern(p.toString().trim(), isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0);
            LOGGER.debug("Created attribute filter pattern [{}] without a mapped return value template", pattern.pattern());
            return Pair.of(pattern, StringUtils.EMPTY);
        }).collect(Collectors.toList());
    }

    private List<Object> filterAndMapAttributeValuesByPattern(final Set<Object> attributeValues, final Pattern pattern, final String returnValue) {
        final List<Object> values = new ArrayList<>();
        attributeValues.forEach(v -> {
            final Matcher matcher = pattern.matcher(v.toString());
            final boolean matches;
            if (isCompleteMatch()) {
                matches = matcher.matches();
            } else {
                matches = matcher.find();
            }
            if (matches) {
                LOGGER.debug("Found a successful match for [{}] while filtering attribute values with [{}]", v.toString(), pattern.pattern());
                final int count = matcher.groupCount();
                if (StringUtils.isNotBlank(returnValue)) {
                    String resultValue = new String(returnValue);
                    for (int i = 1; i <= count; i++) {
                        resultValue = resultValue.replace("$" + i, matcher.group(i));
                    }
                    LOGGER.debug("Final attribute value after template processing for return is [{}]", resultValue);
                    values.add(resultValue);
                } else {
                    values.add(v);
                }
            }
        });
        return values;
    }
}
