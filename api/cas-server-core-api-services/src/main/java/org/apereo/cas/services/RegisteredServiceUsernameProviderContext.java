package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RegisteredServiceUsernameProviderContext}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
@With
@AllArgsConstructor
public class RegisteredServiceUsernameProviderContext {
    private final Principal principal;

    private final Service service;

    private final RegisteredService registeredService;

    private final Map<String, List<Object>> releasingAttributes;

    @Nonnull
    private final ApplicationContext applicationContext;
}
