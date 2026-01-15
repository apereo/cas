package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link DynamoDbConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class DynamoDbConsentRepository implements ConsentRepository {
    @Serial
    private static final long serialVersionUID = 7894919721657056300L;

    private final DynamoDbConsentFacilitator facilitator;

    @Override
    public ConsentDecision findConsentDecision(final Service service,
                                               final RegisteredService registeredService,
                                               final Authentication authentication) {
        return facilitator.find(service, authentication.getPrincipal());
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return facilitator.find(principal);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return facilitator.load();
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        facilitator.save(decision);
        return decision;
    }

    @Override
    public boolean deleteConsentDecision(final long id, final String principal) {
        return facilitator.delete(id, principal);
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        return facilitator.delete(principal);
    }

    @Override
    public void deleteAll() {
        facilitator.removeAll();
    }
}
