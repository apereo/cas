package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

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

    @Nullable
    private final Service service;

    private final RegisteredService registeredService;

    private final Map<String, List<Object>> releasingAttributes;

    @NonNull
    private final ApplicationContext applicationContext;
}
