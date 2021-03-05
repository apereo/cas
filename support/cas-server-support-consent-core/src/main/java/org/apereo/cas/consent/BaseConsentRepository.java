package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RandomUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

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
@Setter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = 1736846688546785564L;

    private Set<ConsentDecision> consentDecisions = new LinkedHashSet<>(0);

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return this.consentDecisions.stream()
            .filter(d -> d.getPrincipal().equals(authentication.getPrincipal().getId())
                && d.getService().equals(service.getId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return this.consentDecisions.stream()
            .filter(d -> d.getPrincipal().equals(principal))
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return new ArrayList<>(this.consentDecisions);
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        val consent = getConsentDecisions()
            .stream()
            .anyMatch(d -> d.getId() == decision.getId());
        if (consent) {
            getConsentDecisions().remove(decision);
        } else {
            decision.setId(RandomUtils.getNativeInstance().nextInt());
        }
        getConsentDecisions().add(decision);
        return decision;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        val decisions = findConsentDecisions(principal);
        val result = decisions.stream().filter(d -> d.getId() == decisionId).findFirst();
        result.ifPresent(value -> this.consentDecisions.remove(value));
        return result.isPresent();
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        return consentDecisions.removeIf(consentDecision -> consentDecision.getPrincipal().equalsIgnoreCase(principal));
    }
}
