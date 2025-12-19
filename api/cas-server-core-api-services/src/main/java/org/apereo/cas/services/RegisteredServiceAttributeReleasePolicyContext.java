package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationContext;
import jakarta.validation.constraints.NotNull;

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

    @NotNull
    private final ApplicationContext applicationContext;

    @Builder.Default
    private final Map<String, List<Object>> releasingAttributes = new LinkedHashMap<>();

    @Builder.Default
    private final Predicate<RegisteredServiceAttributeReleasePolicy> attributeReleasePolicyPredicate = policy -> true;
}
