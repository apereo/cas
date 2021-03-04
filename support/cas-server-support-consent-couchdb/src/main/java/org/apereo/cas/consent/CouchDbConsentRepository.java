package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.couchdb.consent.ConsentDecisionCouchDbRepository;
import org.apereo.cas.couchdb.consent.CouchDbConsentDecision;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return couchDb.findFirstConsentDecision(authentication.getPrincipal().getId(), service.getId());
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        return couchDb.findByPrincipal(principal).stream().map(c -> (ConsentDecision) c).collect(Collectors.toList());
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        return couchDb.getAll().stream().map(c -> (ConsentDecision) c).collect(Collectors.toList());
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        try {
            val consent = couchDb.findFirstConsentDecision(decision);
            var updated = (CouchDbConsentDecision) null;
            if (consent == null) {
                updated = new CouchDbConsentDecision(decision);
                couchDb.add(updated);
            } else {
                updated = consent.copyDetailsFrom(decision);
                couchDb.update(updated);
            }
            return updated;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, "Failure storing consent decision", e);
            return null;
        }
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) {
        try {
            val consent = couchDb.findByPrincipalAndId(principal, id);
            if (consent != null) {
                couchDb.remove(consent);
                return true;
            }
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, "Failure deleting consent decision", e);
        }
        return false;
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        val consent = couchDb.findByPrincipal(principal);
        if (consent != null) {
            consent.forEach(couchDb::remove);
            return true;
        }
        return false;
    }
}
