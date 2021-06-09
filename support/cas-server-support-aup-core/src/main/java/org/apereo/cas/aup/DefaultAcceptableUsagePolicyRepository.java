package org.apereo.cas.aup;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DefaultAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {

    private static final long serialVersionUID = -3059445754626980894L;

    private static final String AUP_ACCEPTED = "AUP_ACCEPTED";

    private final Map<String, Boolean> policyMap = new ConcurrentHashMap<>();

    public DefaultAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                                  final AcceptableUsagePolicyProperties aupProperties) {
        super(ticketRegistrySupport, aupProperties);
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) {
        val storageInfo = getKeyAndMap(requestContext);
        val key = storageInfo.getLeft();
        val map = storageInfo.getRight();
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            throw new AuthenticationException("No authentication could be found in the current context");
        }
        val principal = authentication.getPrincipal();
        if (map.containsKey(key)) {
            val accepted = (boolean) map.getOrDefault(key, Boolean.FALSE) || isUsagePolicyAcceptedBy(principal);
            return new AcceptableUsagePolicyStatus(accepted, principal);
        }
        return AcceptableUsagePolicyStatus.denied(principal);
    }

    @Override
    public boolean submit(final RequestContext requestContext) {
        val storageInfo = getKeyAndMap(requestContext);
        val key = storageInfo.getLeft();
        val map = storageInfo.getRight();
        map.put(key, Boolean.TRUE);
        return map.containsKey(key);
    }

    private Pair<String, Map> getKeyAndMap(final RequestContext requestContext) {
        switch (aupProperties.getInMemory().getScope()) {
            case GLOBAL:
                val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
                return Pair.of(principal.getId(), policyMap);
            case AUTHENTICATION:
            default:
                return Pair.of(AUP_ACCEPTED, requestContext.getFlowScope().asMap());
        }
    }
}
