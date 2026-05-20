package org.apereo.cas.consent;

import module java.base;
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
import org.jspecify.annotations.Nullable;

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
    @Serial
    private static final long serialVersionUID = 1736846688546785564L;

    private Set<ConsentDecision> consentDecisions = Collections.synchronizedSet(new LinkedHashSet<>());

    @Override
    public @Nullable ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                                         final Authentication authentication) {
        return consentDecisions
            .stream()
            .filter(d -> d.getPrincipal().equals(authentication.getPrincipal().getId())
                && d.getService().equals(service.getId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return this.consentDecisions
            .stream()
            .filter(decision -> decision.getPrincipal().equals(principal))
            .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return new ArrayList<>(this.consentDecisions);
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) throws Throwable {
        val consent = getConsentDecisions()
            .stream()
            .anyMatch(consentDecision -> consentDecision.getId() == decision.getId());
        if (consent) {
            getConsentDecisions().remove(decision);
        } else {
            decision.setId(RandomUtils.nextInt());
        }
        getConsentDecisions().add(decision);
        return decision;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) throws Throwable {
        return this.consentDecisions.removeIf(consentDecision -> consentDecision.getId() == decisionId && consentDecision.getPrincipal().equalsIgnoreCase(principal));
    }

    @Override
    public void deleteAll() throws Throwable {
        consentDecisions.clear();
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) throws Throwable {
        return consentDecisions.removeIf(consentDecision -> consentDecision.getPrincipal().equalsIgnoreCase(principal));
    }
}
