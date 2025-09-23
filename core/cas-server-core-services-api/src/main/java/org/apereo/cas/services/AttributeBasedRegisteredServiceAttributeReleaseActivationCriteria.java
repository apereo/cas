package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
@Accessors(chain = true)
public class AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria implements RegisteredServiceAttributeReleaseActivationCriteria {
    @Serial
    private static final long serialVersionUID = 5942510462696845607L;

    @JsonProperty("requiredAttributes")
    private Map<String, Object> requiredAttributes = new TreeMap<>();

    private int order;
    
    private LogicalOperatorTypes operator = LogicalOperatorTypes.AND;

    private boolean reverseMatch;

    @Override
    public boolean shouldActivate(final RegisteredServiceAttributeReleasePolicyContext context) {
        val stream = requiredAttributes.entrySet().stream();
        val attributes = CoreAuthenticationUtils.mergeAttributes(new HashMap<>(context.getPrincipal().getAttributes()), context.getReleasingAttributes());
        LOGGER.debug("Activation criteria will examine attributes [{}]", attributes);
        if (reverseMatch) {
            return stream.noneMatch(entry -> verifyRequiredAttribute(entry, attributes));
        }
        return operator == LogicalOperatorTypes.AND
            ? stream.allMatch(entry -> verifyRequiredAttribute(entry, attributes))
            : stream.anyMatch(entry -> verifyRequiredAttribute(entry, attributes));
    }

    protected boolean verifyRequiredAttribute(final Map.Entry<String, Object> entry,
                                              final Map<String, List<Object>> attributes) {
        val currentValues = ObjectUtils.getIfNull(attributes.get(entry.getKey()), List.of());
        val requiredValues = CollectionUtils.toCollection(entry.getValue());
        val pattern = RegexUtils.concatenate(requiredValues, true);
        LOGGER.debug("Checking activation criteria [{}] against [{}] with pattern [{}] for attribute [{}]",
            requiredValues, currentValues, pattern, entry.getKey());
        return currentValues.stream().map(Object::toString).anyMatch(pattern.asPredicate());
    }
}
