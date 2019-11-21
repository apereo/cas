package org.apereo.cas.services.support;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceMutantRegexAttributeFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisteredServiceMutantRegexAttributeFilter extends RegisteredServiceMappedRegexAttributeFilter {

    private static final long serialVersionUID = 543145306984660628L;

    @Override
    public Map<String, List<Object>> filter(final Map<String, List<Object>> givenAttributes) {
        val attributesToRelease = new HashMap<String, List<Object>>();
        givenAttributes.entrySet().stream().filter(filterProvidedGivenAttributes()).forEach(entry -> {
            val attributeName = entry.getKey();
            if (getPatterns().containsKey(attributeName)) {
                val attributeValues = CollectionUtils.toCollection(entry.getValue());
                LOGGER.trace("Found attribute [{}] in pattern definitions with value(s) [{}]", attributeName, attributeValues);
                val patterns = createPatternsAndReturnValue(attributeName);
                var finalValues = patterns
                    .stream()
                    .map(patternDefinition -> {
                        val pattern = patternDefinition.getLeft();
                        LOGGER.trace("Found attribute [{}] in the pattern definitions. Processing pattern [{}]", attributeName, pattern.pattern());
                        var filteredValues = filterAndMapAttributeValuesByPattern(attributeValues, pattern, patternDefinition.getValue());
                        LOGGER.debug("Filtered attribute values for [{}] are [{}]", attributeName, filteredValues);
                        return filteredValues;
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
                if (finalValues.isEmpty()) {
                    LOGGER.trace("Attribute [{}] has no values remaining and shall be excluded", attributeName);
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
        val patternDef = getPatterns().get(attributeName);
        val patternAndReturnVal = new ArrayList<Object>(CollectionUtils.toCollection(patternDef));
        return patternAndReturnVal
            .stream()
            .map(this::mapPattern)
            .collect(Collectors.toList());
    }

    private List<Object> filterAndMapAttributeValuesByPattern(final Set<Object> attributeValues, final Pattern pattern, final String returnValue) {
        val values = new ArrayList<Object>(attributeValues.size());
        attributeValues.forEach(v -> {
            val matcher = pattern.matcher(v.toString());
            val matches = isCompleteMatch() ? matcher.matches() : matcher.find();
            if (matches) {
                LOGGER.debug("Found a successful match for [{}] while filtering attribute values with [{}]", v.toString(), pattern.pattern());
                if (StringUtils.isNotBlank(returnValue)) {
                    val count = matcher.groupCount();
                    var resultValue = returnValue;
                    for (var i = 1; i <= count; i++) {
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

    private Pair<Pattern, String> mapPattern(final Object p) {
        val patternValue = p.toString();
        val index = patternValue.indexOf("->");
        if (index != -1) {
            val patternStr = patternValue.substring(0, index).trim();
            val pattern = RegexUtils.createPattern(patternStr, isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0);
            val returnValue = patternValue.substring(index + 2).trim();
            LOGGER.debug("Created attribute filter pattern [{}] with the mapped return value template [{}]", patternStr, returnValue);
            return Pair.of(pattern, returnValue);
        }
        val pattern = RegexUtils.createPattern(patternValue.trim(), isCaseInsensitive() ? Pattern.CASE_INSENSITIVE : 0);
        LOGGER.debug("Created attribute filter pattern [{}] without a mapped return value template", pattern.pattern());
        return Pair.of(pattern, StringUtils.EMPTY);
    }
}
