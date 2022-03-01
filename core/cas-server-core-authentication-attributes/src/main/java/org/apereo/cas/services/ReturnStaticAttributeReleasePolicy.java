package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    private static final long serialVersionUID = 1239257723778012771L;

    private Map<String, List<Object>> allowedAttributes = new TreeMap<>();

    @JsonCreator
    public ReturnStaticAttributeReleasePolicy(
        @JsonProperty("allowedAttributes")
        final Map<String, List<Object>> attributes) {
        this.allowedAttributes = attributes;
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> resolvedAttributes) {
        return this.allowedAttributes;
    }
}
