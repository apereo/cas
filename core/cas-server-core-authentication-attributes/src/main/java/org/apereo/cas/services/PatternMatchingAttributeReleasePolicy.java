package org.apereo.cas.services;

import org.apereo.cas.util.RegexUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link PatternMatchingAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class PatternMatchingAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -2168544657991721919L;

    private static final Pattern PATTERN_TRANSFORM_GROUPS = RegexUtils.createPattern("\\$\\{(\\d+)\\}");

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Rule> allowedAttributes = new TreeMap<>();

    @JsonCreator
    public PatternMatchingAttributeReleasePolicy(
        @JsonProperty("allowedAttributes") final Map<String, Rule> attributes) {
        this.allowedAttributes = attributes;
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        return allowedAttributes
            .entrySet()
            .stream()
            .filter(entry -> attributes.containsKey(entry.getKey()))
            .map(entry -> {
                val rule = entry.getValue();

                val valuePattern = RegexUtils.createPattern(rule.getPattern());
                val transformPattern = PATTERN_TRANSFORM_GROUPS.matcher(rule.getTransform());

                val attributeValues = attributes.get(entry.getKey());
                val transformedValues = attributeValues
                    .stream()
                    .map(value -> {
                        var transformedValue = rule.getTransform();
                        val matcher = valuePattern.matcher(value.toString());
                        if (matcher.find()) {
                            while (transformPattern.find()) {
                                val group = Integer.parseInt(transformPattern.group(1));
                                val target = String.format("${%s}", group);
                                transformedValue = transformedValue.replace(target, matcher.group(group));
                            }
                        }
                        transformPattern.reset();
                        return transformedValue;
                    })
                    .collect(Collectors.<Object>toList());
                return Pair.of(entry.getKey(), transformedValues);
            })
            .filter(pair -> !pair.getValue().isEmpty())
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @ToString
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Accessors(chain = true)
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @EqualsAndHashCode
    public static class Rule implements Serializable {
        @Serial
        private static final long serialVersionUID = 3111910879481087570L;

        private String pattern;

        private String transform;
    }
}
