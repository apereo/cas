package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link AttributeBasedRegisteredServiceAccessStrategyActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
@Accessors(chain = true)
public class AttributeBasedRegisteredServiceAccessStrategyActivationCriteria implements RegisteredServiceAccessStrategyActivationCriteria {
    @Serial
    private static final long serialVersionUID = 5228603912161923218L;

    @JsonProperty("requiredAttributes")
    private Map<String, Object> requiredAttributes = new TreeMap<>();

    private int order;

    private boolean allowIfInactive = true;

    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    @Override
    public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) {
        val stream = requiredAttributes.entrySet().stream();
        return operator == LogicalOperatorTypes.AND
            ? stream.allMatch(entry -> verifyRequiredAttribute(entry, request))
            : stream.anyMatch(entry -> verifyRequiredAttribute(entry, request));
    }

    protected boolean verifyRequiredAttribute(
        final Map.Entry<String, Object> entry,
        final RegisteredServiceAccessStrategyRequest request) {
        val currentValues = ObjectUtils.getIfNull(request.getAttributes().get(entry.getKey()), List.of());
        val requiredValues = CollectionUtils.toCollection(entry.getValue());
        val pattern = RegexUtils.concatenate(requiredValues, true);
        LOGGER.debug("Checking activation criteria [{}] against [{}] with pattern [{}] for attribute [{}]",
            requiredValues, currentValues, pattern, entry.getKey());
        return pattern.equals(RegexUtils.MATCH_NOTHING_PATTERN)
            ? currentValues.stream().anyMatch(requiredValues::contains)
            : currentValues.stream().map(Object::toString).anyMatch(pattern.asPredicate());
    }
}
