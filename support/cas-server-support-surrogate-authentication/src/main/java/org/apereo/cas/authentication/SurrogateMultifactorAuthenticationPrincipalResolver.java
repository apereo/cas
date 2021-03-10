package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import lombok.val;

/**
 * This is {@link SurrogateMultifactorAuthenticationPrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class SurrogateMultifactorAuthenticationPrincipalResolver implements MultifactorAuthenticationPrincipalResolver {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean supports(final Principal principal) {
        return principal instanceof SurrogatePrincipal;
    }

    @Override
    public Principal resolve(final Principal principal) {
        val surrogate = (SurrogatePrincipal) principal;
        return surrogate.getPrimary();
    }
}
