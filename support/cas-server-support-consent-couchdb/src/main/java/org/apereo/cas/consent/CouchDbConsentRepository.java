package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.consent.CouchDbConsentDecision;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.DbAccessException;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbConsentRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class CouchDbConsentRepository implements ConsentRepository {

    private static final long serialVersionUID = 5058836218210655958L;

    /**
     * CouchDb DAO.
     */
    private final transient ConsentDecisionCouchDbRepository couchDb;

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService, final Authentication authentication) {
        return couchDb.findFirstConsentDecision(authentication.getPrincipal().getId(), service.getId());
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        return couchDb.findByPrincipal(principal).stream().map(c-> (ConsentDecision) c).collect(Collectors.toList());
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        return couchDb.getAll().stream().map(c -> (ConsentDecision) c).collect(Collectors.toList());
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        try {
            val consent = couchDb.findFirstConsentDecision(decision);
            if (consent == null) {
                couchDb.add(new CouchDbConsentDecision(decision));
            } else {
                couchDb.update(consent.copyDetailsFrom(decision));
            }
            return true;
        } catch (final DbAccessException e) {
            LOGGER.warn("Failure storing consent decision", e);
            return false;
        }
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) {
        try {
            val consent = couchDb.findByPrincipalAndId(principal, id);
            if (consent == null) {
                LOGGER.debug("Decision to be deleted not found [{}] [{}]", principal, id);
            } else {
                couchDb.remove(consent);
                return true;
            }
        } catch (final DbAccessException e) {
            LOGGER.warn("Failure deleting consent decision", e);
        }
        return false;
    }
}
