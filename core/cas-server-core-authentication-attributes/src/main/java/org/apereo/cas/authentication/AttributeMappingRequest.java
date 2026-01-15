package org.apereo.cas.authentication;

import module java.base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link AttributeMappingRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@With
@AllArgsConstructor
public class AttributeMappingRequest {
    private final String attributeName;

    private final String mappedAttributeName;

    @Builder.Default
    private final Map<String, List<Object>> resolvedAttributes = new TreeMap<>();

    @Builder.Default
    private final List<Object> attributeValue = new ArrayList<>();
}
