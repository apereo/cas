package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link AttributeDefinitionResolutionContext}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@RequiredArgsConstructor
@With
public class AttributeDefinitionResolutionContext {
    @Builder.Default
    private final List<Object> attributeValues = new ArrayList<>();

    private final String scope;

    private final Principal principal;

    private final RegisteredService registeredService;

    @Builder.Default
    private final Map<String, List<Object>> attributes = new HashMap<>();

    private final Service service;
}
