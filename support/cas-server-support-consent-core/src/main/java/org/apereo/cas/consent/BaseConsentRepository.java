package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RandomUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link BaseConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 1736846688546785564L;
    
    private Set<ConsentDecision> consentDecisions;

    public BaseConsentRepository() {
        this.consentDecisions = new LinkedHashSet<>();
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return this.consentDecisions
                .stream()
                .filter(d -> d.getPrincipal().equals(authentication.getPrincipal().getId())
                        && d.getService().equals(service.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions(final String principal) {
        return this.consentDecisions
                .stream()
                .filter(d -> d.getPrincipal().equals(principal))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ConsentDecision> findConsentDecisions() {
        return new ArrayList<>(this.consentDecisions);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final ConsentDecision consent = getConsentDecisions()
                .stream()
                .filter(d -> d.getId() == decision.getId())
                .findFirst()
                .orElse(null);
        if (consent != null) {
            getConsentDecisions().remove(decision);
        } else {
            decision.setId(Math.abs(RandomUtils.getInstanceNative().nextInt()));
        }
        getConsentDecisions().add(decision);
        return true;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        final Collection<ConsentDecision> decisions = findConsentDecisions(principal);
        return this.consentDecisions.remove(decisions
            .stream()
            .filter(d -> d.getId() == decisionId)
            .findFirst().get());
    }

    protected Set<ConsentDecision> getConsentDecisions() {
        return this.consentDecisions;
    }

    protected void setConsentDecisions(final Set<ConsentDecision> consentDecisions) {
        this.consentDecisions = consentDecisions;
    }
}
