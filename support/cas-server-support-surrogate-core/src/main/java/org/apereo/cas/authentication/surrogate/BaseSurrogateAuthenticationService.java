package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import java.util.Optional;

/**
 * This is {@link BaseSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSurrogateAuthenticationService implements SurrogateAuthenticationService {
    protected final ServicesManager servicesManager;
    protected final CasConfigurationProperties casProperties;

    @Override
    public final boolean canImpersonate(final String surrogate, final Principal principal, final Optional<? extends Service> service) throws Throwable {
        return surrogate.equalsIgnoreCase(principal.getId())
            || isPrincipalAttributeAuthorized(surrogate, principal, service)
            || isWildcardedAccount(surrogate, principal, service)
            || canImpersonateInternal(surrogate, principal, service);
    }

    protected boolean isPrincipalAttributeAuthorized(final String surrogate, final Principal principal, final Optional<? extends Service> service) {
        principal.getAttributes().get("membership");
        return false;
    }

    protected abstract boolean canImpersonateInternal(String surrogate, Principal principal, Optional<? extends Service> service) throws Throwable;
}
