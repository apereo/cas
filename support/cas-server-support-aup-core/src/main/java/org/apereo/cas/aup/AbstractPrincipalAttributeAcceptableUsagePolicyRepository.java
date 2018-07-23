package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AbstractPrincipalAttributeAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPrincipalAttributeAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1883808902502739L;

    /**
     * Ticket registry support.
     */
    protected final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Single-valued attribute in LDAP that describes whether the policy
     * has been accepted. Its value must match either TRUE/FALSE.
     */
    protected final String aupAttributeName;

    @Override
    public Pair<Boolean, Principal> verify(final RequestContext requestContext, final Credential credential) {
        @NonNull
        val principal = WebUtils.getPrincipalFromRequestContext(requestContext, this.ticketRegistrySupport);

        if (isUsagePolicyAcceptedBy(principal)) {
            LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
            return Pair.of(Boolean.TRUE, principal);
        }

        LOGGER.warn("Usage policy has not been accepted by [{}]", principal.getId());
        return Pair.of(Boolean.FALSE, principal);
    }

    /**
     * Is usage policy accepted by user?
     * Looks into the attributes collected by the principal to find {@link #aupAttributeName}.
     * If the attribute contains {@code true}, then the policy is determined as accepted.
     *
     * @param principal the principal
     * @return true if accepted, false otherwise.
     */
    protected boolean isUsagePolicyAcceptedBy(final Principal principal) {
        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);

        if (attributes != null && attributes.containsKey(this.aupAttributeName)) {
            val value = CollectionUtils.toCollection(attributes.get(this.aupAttributeName));
            LOGGER.debug("Evaluating attribute value [{}] found for [{}]", value, this.aupAttributeName);
            return value.stream().anyMatch(v -> v.toString().equalsIgnoreCase(Boolean.TRUE.toString()));
        }
        return false;
    }
}
