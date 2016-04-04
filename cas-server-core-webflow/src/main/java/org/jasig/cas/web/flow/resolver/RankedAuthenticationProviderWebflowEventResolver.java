package org.jasig.cas.web.flow.resolver;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationContextValidator;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.util.Pair;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RankedAuthenticationProviderWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("rankedAuthenticationProviderWebflowEventResolver")
public class RankedAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {
    

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    
    @Autowired
    @Qualifier("authenticationContextValidator")
    private AuthenticationContextValidator authenticationContextValidator;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final String tgt = WebUtils.getTicketGrantingTicketId(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);

        if (service == null) {
            logger.debug("No service is available to determine event for principal");
            return resumeFlow();
        }

        if (StringUtils.isBlank(tgt)) {
            logger.trace("TGT is blank; proceed with flow normally.");
            return resumeFlow();
        }
        final Authentication authentication = this.ticketRegistrySupport.getAuthenticationFrom(tgt);
        if (authentication == null) {
            logger.trace("TGT has no authentication and is blank; proceed with flow normally.");
            return resumeFlow();
        }

        final AuthenticationResultBuilder builder =
                this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        final Event event = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            logger.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow();
        }

        if (event.getId().equals(CasWebflowConstants.TRANSITION_ID_ERROR)
            || event.getId().equals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
            || event.getId().equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            logger.debug("Returning webflow event as {}", event.getId());
            return ImmutableSet.of(event);
        }

        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result =
                this.authenticationContextValidator.validate(authentication, event.getId(), service);

        if (result.getFirst()) {
            return resumeFlow();
        }

        if (result.getSecond().isPresent()) {
            return ImmutableSet.of(validateEventIdForMatchingTransitionInContext(event.getId(), context,
                    buildEventAttributeMap(authentication.getPrincipal(), service, result.getSecond().get())));
        }
        logger.warn("The authentication context cannot be satisfied and the requested event {} is unrecognized", event.getId());
        return ImmutableSet.of(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));

    }

    private Set<Event> resumeFlow() {
        return ImmutableSet.of(new EventFactorySupport().success(this));
    }
}
