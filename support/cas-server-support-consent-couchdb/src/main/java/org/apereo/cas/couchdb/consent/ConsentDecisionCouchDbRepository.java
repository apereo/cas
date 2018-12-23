package org.apereo.cas.couchdb.consent;

import org.apereo.cas.consent.ConsentDecision;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;

import java.util.List;

/**
 * This is {@link ConsentDecisionCouchDbRepository}. DAO for CouchDb stored {@link ConsentDecision}s.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { emit(doc._id, doc) }")
public class ConsentDecisionCouchDbRepository extends CouchDbRepositorySupport<CouchDbConsentDecision> {
    public ConsentDecisionCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbConsentDecision.class, db, createIfNotExists);
    }

    /**
     * Find {@link ConsentDecision} belonging to a principal.
     * @param principal to search for.
     * @return All entries for the given principal.
     */
    @GenerateView
    public List<CouchDbConsentDecision> findByPrincipal(final String principal) {
        return queryView("by_principal", principal);
    }

    /**
     * Find all consent decisions for a given principal, service pair. Should only be one.
     * @param principal User to search for.
     * @param service Service name to search for.
     * @return Consent decisions matching the given principal and service names.
     */
    @View(name = "by_consent_decision", map = "function(doc) {emit([doc.principal, doc.service], doc)}")
    public List<CouchDbConsentDecision> findConsentDecision(final String principal, final String service) {
        val view = createQuery("by_consent_decision").key(ComplexKey.of(principal, service)).includeDocs(true);
        return db.queryView(view, CouchDbConsentDecision.class);
    }

    /**
     * Find the first consent decision for a given principal, service pair. Should only be one of them anyway.
     * @param principal User to search for.
     * @param service Service name to search for.
     * @return Consent decision matching the given principal and service names.
     */
    public CouchDbConsentDecision findFirstConsentDecision(final String principal, final String service) {
        val view = createQuery("by_consent_decision").key(ComplexKey.of(principal, service)).limit(1).includeDocs(true);
        return db.queryView(view, CouchDbConsentDecision.class).stream().findFirst().orElse(null);
    }

    /**
     * Find the first consent decision for a given {@link ConsentDecision}. Should only be one of them anyway.
     * @param consent Consent decision to search for
     * @return Consent decision matching the given principal and service names in the consent decision provided.
     */
    public CouchDbConsentDecision findFirstConsentDecision(final ConsentDecision consent) {
        return findFirstConsentDecision(consent.getPrincipal(), consent.getService());
    }

    /**
     * Find a consent decision by +long+ ID and principal name. For CouchDb, this ID is randomly generated and the pair should be unique, with a very high
     * probability, but is not guaranteed. This method is mostly only used by tests.
     * @param principal User to search for.
     * @param id decision id to search for.
     * @return First consent decision matching principal and id.
     */
    @View(name = "by_principal_and_id", map = "function(doc) {emit([doc.principal, doc.id], doc)}")
    public CouchDbConsentDecision findByPrincipalAndId(final String principal, final long id) {
        val view = createQuery("by_principal_and_id").key(ComplexKey.of(principal, id)).limit(1).includeDocs(true);
        return db.queryView(view, CouchDbConsentDecision.class).stream().findFirst().orElse(null);
    }
}
