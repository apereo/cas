package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Credential;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link DefaultAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("defaultAcceptableUsagePolicyRepository")
public class DefaultAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    private static final long serialVersionUID = -3059445754626980894L;

    private final Map<String, Boolean> policyMap = new ConcurrentHashMap<>();

    @Override
    public boolean verify(final RequestContext requestContext, final Credential credential) {
        final String key = credential.getId();
        synchronized (this.policyMap) {
            if (this.policyMap.containsKey(key)) {
                return this.policyMap.get(key);
            }
        }
        return false;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        synchronized (this.policyMap) {
            this.policyMap.put(credential.getId(), Boolean.TRUE);
            return this.policyMap.containsKey(credential.getId());
        }
    }

}
