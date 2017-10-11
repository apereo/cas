package org.apereo.cas.aup;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {

    private static final long serialVersionUID = -3059445754626980894L;

    private final Map<String, Boolean> policyMap = new ConcurrentHashMap<>();

    public DefaultAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport) {
        super(ticketRegistrySupport, null);
    }

    @Override
    public Pair<Boolean, Principal> verify(final RequestContext requestContext, final Credential credential) {
        final String key = credential.getId();
        final Principal principal = WebUtils.getPrincipalFromRequestContext(requestContext, this.ticketRegistrySupport);
        if (this.policyMap.containsKey(key)) {
            return Pair.of(this.policyMap.get(key), principal);
        }
        return Pair.of(Boolean.FALSE, principal);
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        this.policyMap.put(credential.getId(), Boolean.TRUE);
        return this.policyMap.containsKey(credential.getId());
    }

}
