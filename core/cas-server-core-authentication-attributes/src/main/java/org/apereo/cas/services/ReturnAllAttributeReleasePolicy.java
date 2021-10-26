package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Return all attributes for the service, regardless of service settings.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Getter
@Setter
@NoArgsConstructor
public class ReturnAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 5519257723778012771L;

    @JsonProperty
    private Set<String> excludedAttributes;

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> resolvedAttributes) {
        if (excludedAttributes != null) {
            excludedAttributes.forEach(resolvedAttributes::remove);
        }
        return resolvedAttributes;
    }
}
