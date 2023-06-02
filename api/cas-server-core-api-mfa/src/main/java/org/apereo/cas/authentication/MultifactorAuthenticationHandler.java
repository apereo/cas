package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * This is {@link MultifactorAuthenticationHandler}.
 * It represents the common operations that a given handler
 * might expose or implement for multifactor authentication,
 * or can be treated as a marker interface.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface MultifactorAuthenticationHandler extends AuthenticationHandler {
    /**
     * Gets multifactor provider id linked to this handler.
     *
     * @return the multifactor provider id
     */
    ObjectProvider<? extends MultifactorAuthenticationProvider> getMultifactorAuthenticationProvider();


    /**
     * Resolve principal.
     *
     * @param applicationContext the application context
     * @param principal          the principal
     * @return the principal
     */
    default Principal resolvePrincipal(final ApplicationContext applicationContext, final Principal principal) {
        val resolvers = applicationContext.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class).values();
        return resolvers
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .filter(resolver -> resolver.supports(principal))
            .findFirst()
            .map(r -> r.resolve(principal))
            .orElseThrow(() -> new IllegalStateException("Unable to resolve principal for multifactor authentication"));
    }
}
