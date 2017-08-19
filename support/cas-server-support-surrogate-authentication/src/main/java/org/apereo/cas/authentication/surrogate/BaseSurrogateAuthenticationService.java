package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;

/**
 * This is {@link BaseSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseSurrogateAuthenticationService implements SurrogateAuthenticationService {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    public BaseSurrogateAuthenticationService(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public final boolean canAuthenticateAs(final String surrogate, final Principal principal, final Service service) {
        return canAuthenticateAsInternal(surrogate, principal);
    }

    /**
     * Can principal authenticate as surrogate.
     *
     * @param surrogate the surrogate
     * @param principal the principal
     * @return the boolean
     */
    protected abstract boolean canAuthenticateAsInternal(String surrogate, Principal principal);
}
