package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.couchdb.surrogate.SurrogateAuthorizationCouchDbRepository;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This is {@link SurrogateCouchDbAuthenticationService}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class SurrogateCouchDbAuthenticationService extends BaseSurrogateAuthenticationService {

    private SurrogateAuthorizationCouchDbRepository couchDb;

    public SurrogateCouchDbAuthenticationService(final SurrogateAuthorizationCouchDbRepository couchDb, final ServicesManager servicesManager) {
        super(servicesManager);
        this.couchDb = couchDb;
    }

    @Override
    protected boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Service service) {
        return !couchDb.findBySurrogatePrincipal(surrogate, principal.getId()).isEmpty();
    }

    @Override
    public List<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        return couchDb.findByPrincipal(username);
    }
}
