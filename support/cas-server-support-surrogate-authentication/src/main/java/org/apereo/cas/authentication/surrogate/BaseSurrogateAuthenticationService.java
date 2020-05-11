package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * This is {@link BaseSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public abstract class BaseSurrogateAuthenticationService implements SurrogateAuthenticationService {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    @Override
    public final boolean canAuthenticateAs(final String surrogate, final Principal principal, final Optional<Service> service) {
        return canAuthenticateAsInternal(surrogate, principal, service);
    }

    /**
     * Can principal authenticate as surrogate.
     *
     * @param surrogate the surrogate
     * @param principal the principal
     * @param service   the service
     * @return true/false
     */
    protected abstract boolean canAuthenticateAsInternal(String surrogate, Principal principal, Optional<Service> service);
}
