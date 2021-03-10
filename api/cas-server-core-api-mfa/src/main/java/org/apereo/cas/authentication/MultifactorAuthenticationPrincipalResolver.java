package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import org.springframework.core.Ordered;

/**
 * This is {@link MultifactorAuthenticationPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.3.3
 */
@FunctionalInterface
public interface MultifactorAuthenticationPrincipalResolver extends Ordered {
    /**
     * No op multifactor authentication principal resolver.
     *
     * @return the multifactor authentication principal resolver
     */
    static MultifactorAuthenticationPrincipalResolver identical() {
        return principal -> principal;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Supports resolution?
     *
     * @param principal the principal
     * @return the boolean
     */
    default boolean supports(final Principal principal) {
        return principal != null;
    }

    /**
     * Resolve principal.
     *
     * @param principal the principal
     * @return the principal
     */
    Principal resolve(Principal principal);
}
