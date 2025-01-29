package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ApplicationContext;
import java.util.Map;

/**
 * This is {@link RegisteredServicePrincipalAccessStrategyEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
public interface RegisteredServicePrincipalAccessStrategyEnforcer {

    /**
     * The default bean name.
     */
    String BEAN_NAME = "principalAccessStrategyEnforcer";

    /**
     * Authorize boolean.
     *
     * @param context the context
     * @return the boolean
     */
    Boolean authorize(PrincipalAccessStrategyContext context);

    @SuperBuilder
    @Getter
    @With
    @AllArgsConstructor
    class PrincipalAccessStrategyContext {
        private final RegisteredService registeredService;
        private final String principalId;
        private final Map principalAttributes;
        private final Service service;
        private final ApplicationContext applicationContext;
    }
}
