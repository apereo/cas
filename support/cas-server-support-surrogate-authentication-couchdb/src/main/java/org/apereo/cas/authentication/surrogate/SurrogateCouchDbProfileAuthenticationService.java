package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.couchdb.core.CouchDbProfileDocument;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uses {@link CouchDbProfileDocument} to store a list of the principals a user may surrogate.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class SurrogateCouchDbProfileAuthenticationService extends BaseSurrogateAuthenticationService {

    private ProfileCouchDbRepository couchDb;
    private String surrogatePrincipalsAttribute;

    public SurrogateCouchDbProfileAuthenticationService(final ProfileCouchDbRepository couchDb, final String surrogatePrincipalsAttribute, final ServicesManager servicesManager) {
        super(servicesManager);
        this.couchDb = couchDb;
        this.surrogatePrincipalsAttribute = surrogatePrincipalsAttribute;
    }

    @Override
    protected boolean canAuthenticateAsInternal(final String surrogate, final Principal principal, final Service service) {
        LOGGER.warn("User [{}] attempting surrogate for [{}] at [{}]", principal.getId(), surrogate, service.getId());
        val user = couchDb.findByUsername(principal.getId());
        LOGGER.debug("User [{}]", user);
        if (user == null) {
            LOGGER.info("User [{}] not found for surrogacy.", principal.getId());
            return false;
        }
        val users = user.getAttribute(surrogatePrincipalsAttribute);
        LOGGER.debug("Users elegible for [{}]", users);
        if (users == null) {
            LOGGER.info("User [{}] has no surrogate principals entry.", principal.getId());
            return false;
        }

        val userList = CollectionUtils.toCollection(users);
        if (userList.isEmpty()) {
            LOGGER.info("User [{}] is not an eligible surrogate.", principal.getId());
            return false;
        }
        if (userList.contains(surrogate)) {
            LOGGER.warn("User [{}] becoming surrogate for [{}] at [{}]", principal.getId(), surrogate, service.getId());
            return true;
        }
        return false;
    }

    @Override
    public List<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        LOGGER.debug("Listing eligible accounts for user [{}].", username);
        val user = couchDb.findByUsername(username);
        if (user == null) {
            LOGGER.debug("User [{}] not found for surrogacy.", username);
            return Collections.EMPTY_LIST;
        }
        val users = user.getAttribute(surrogatePrincipalsAttribute);
        if (users == null) {
            LOGGER.debug("User [{}] has no surrogate principals entry.", user.getUsername());
            return Collections.EMPTY_LIST;
        }
        return CollectionUtils.toCollection(users, ArrayList.class);
    }
}
