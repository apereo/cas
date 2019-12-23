package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.couchdb.surrogate.SurrogateAuthorizationCouchDbRepository;
import org.apereo.cas.services.ServicesManager;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link SurrogateCouchDbAuthenticationService}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class SurrogateCouchDbAuthenticationService extends BaseSurrogateAuthenticationService {

    private final SurrogateAuthorizationCouchDbRepository couchDb;

    public SurrogateCouchDbAuthenticationService(final SurrogateAuthorizationCouchDbRepository couchDb, final ServicesManager servicesManager) {
        super(servicesManager);
        this.couchDb = couchDb;
    }

    @Override
    protected boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Optional<Service> service) {
        return !couchDb.findBySurrogatePrincipal(surrogate, principal.getId()).isEmpty();
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        return couchDb.findByPrincipal(username);
    }
}
