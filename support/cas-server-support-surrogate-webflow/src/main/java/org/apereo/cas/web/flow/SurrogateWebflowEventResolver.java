package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link SurrogateWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateWebflowEventResolver extends AbstractCasWebflowEventResolver {
    /**
     * Internal flag to indicate whether surrogate account selection is requested.
     */
    public static final String CONTEXT_ATTRIBUTE_REQUEST_SURROGATE = "requestSurrogateAccount";

    private final SurrogateAuthenticationService surrogateService;

    public SurrogateWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext,
                                         final SurrogateAuthenticationService surrogateService) {
        super(webflowEventResolutionConfigurationContext);
        this.surrogateService = surrogateService;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext requestContext) {
        if (requestContext.getFlowScope().getBoolean(CONTEXT_ATTRIBUTE_REQUEST_SURROGATE, Boolean.FALSE)) {
            requestContext.getFlowScope().remove(CONTEXT_ATTRIBUTE_REQUEST_SURROGATE);
            if (loadSurrogates(requestContext)) {
                return CollectionUtils.wrapSet(new Event(this, SurrogateWebflowConfigurer.TRANSITION_ID_SURROGATE_VIEW));
            }
        }
        return null;
    }

    private boolean loadSurrogates(final RequestContext requestContext) {
        val c = WebUtils.getCredential(requestContext);
        if (c instanceof UsernamePasswordCredential) {
            val username = c.getId();
            val surrogates = surrogateService.getEligibleAccountsForSurrogateToProxy(username);
            if (!surrogates.isEmpty()) {
                surrogates.add(username);
                requestContext.getFlowScope().put("surrogates", surrogates);
                return true;
            }
        }
        return false;
    }
}
