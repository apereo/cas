package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.couchdb.core.CouchDbProfileDocument;
import org.apereo.cas.couchdb.core.ProfileCouchDbRepository;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.UpdateConflictException;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

/**
 * This is {@link CouchDbAcceptableUsagePolicyRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class CouchDbAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {

    private static final long serialVersionUID = -2391630070546362552L;

    private final transient ProfileCouchDbRepository couchDb;

    private final int conflictRetries;

    public CouchDbAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                  final AcceptableUsagePolicyProperties properties,
                                                  final ProfileCouchDbRepository couchDb, final int conflictRetries) {
        super(ticketRegistrySupport, properties);
        this.couchDb = couchDb;
        this.conflictRetries = conflictRetries;
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getPrincipalFromRequestContext(requestContext, this.ticketRegistrySupport);

        if (principal != null) {
            if (isUsagePolicyAcceptedBy(principal)) {
                LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
                return AcceptableUsagePolicyStatus.accepted(principal);
            }
            LOGGER.debug("Usage policy has not been accepted by [{}] in the resolved principal", principal.getId());
        }

        val profile = couchDb.findByUsername(credential.getId());
        var accepted = false;
        if (profile != null) {
            val values = CollectionUtils.toCollection(profile.getAttribute(aupProperties.getCore().getAupAttributeName()));
            accepted = CollectionUtils.firstElement(values).map(value -> (Boolean) value).orElse(Boolean.FALSE);
        }
        if (accepted) {
            LOGGER.debug("Usage policy has been accepted by [{}]", profile.getUsername());
        }
        return new AcceptableUsagePolicyStatus(accepted, principal);
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        val username = credential.getId();
        val profile = couchDb.findByUsername(username);
        if (profile == null) {
            val doc = new CouchDbProfileDocument(username, null,
                CollectionUtils.wrap(aupProperties.getCore().getAupAttributeName(), List.of(Boolean.TRUE)));
            couchDb.add(doc);
            return true;
        }
        var success = false;
        profile.setAttribute(aupProperties.getCore().getAupAttributeName(), List.of(Boolean.TRUE));
        UpdateConflictException exception = null;
        for (var retries = 0; !success && retries < conflictRetries; retries++) {
            try {
                couchDb.update(profile);
                success = true;
            } catch (final Exception e) {
                LOGGER.debug("Could not update AUP acceptance for [{}].\n[{}]", username, exception);
            }
        }
        if (success) {
            LOGGER.debug("Successfully updated AUP for [{}].", profile.getUsername());
        }
        return success;
    }
}
