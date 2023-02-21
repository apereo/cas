package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Return static attributes for the service.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Getter
@Setter
@NoArgsConstructor
public class ReturnStaticAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 1239257723778012771L;

    @JsonProperty("allowedAttributes")
    @ExpressionLanguageCapable
    private Map<String, List<Object>> allowedAttributes = new TreeMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> resolvedAttributes) {
        return allowedAttributes
            .entrySet()
            .stream()
            .map(entry -> {
                val values = entry.getValue()
                    .stream()
                    .map(value -> SpringExpressionLanguageValueResolver.getInstance().resolve(value.toString()))
                    .collect(Collectors.toList());
                return Pair.of(entry.getKey(), values);
            })
            .collect(Collectors.toMap(Pair::getKey, entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }
}
