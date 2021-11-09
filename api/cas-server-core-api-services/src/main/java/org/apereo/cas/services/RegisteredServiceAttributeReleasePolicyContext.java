package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RegisteredServiceAttributeReleasePolicyContext}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
public class RegisteredServiceAttributeReleasePolicyContext {
    private final Principal principal;

    private final Service service;

    private final RegisteredService registeredService;

    @Builder.Default
    private final Map<String, List<Object>> releasingAttributes = new LinkedHashMap<>();
}
