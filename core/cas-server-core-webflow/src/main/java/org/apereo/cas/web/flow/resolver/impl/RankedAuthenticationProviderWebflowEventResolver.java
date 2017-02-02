package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationContextValidator;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.Pair;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
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
public class RankedAuthenticationProviderWebflowEventResolver extends AbstractCasWebflowEventResolver {
    
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

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

        final Credential credential = WebUtils.getCredential(context);
        final AuthenticationResultBuilder builder =
                this.authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        WebUtils.putAuthentication(authentication, context);

        final Event event = this.initialAuthenticationAttemptWebflowEventResolver.resolveSingle(context);
        if (event == null) {
            logger.trace("Request does not indicate a requirement for authentication policy; proceed with flow normally.");
            return resumeFlow();
        }

        final String id = event.getId();
        
        if (id.equals(CasWebflowConstants.TRANSITION_ID_ERROR)
            || id.equals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
            || id.equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
            logger.debug("Returning webflow event as {}", id);
            return ImmutableSet.of(event);
        }

        final Pair<Boolean, Optional<MultifactorAuthenticationProvider>> result =
                this.authenticationContextValidator.validate(authentication, id, service);

        if (result.getFirst()) {
            return resumeFlow();
        }

        if (result.getSecond().isPresent()) {
            return ImmutableSet.of(validateEventIdForMatchingTransitionInContext(id, context,
                    buildEventAttributeMap(authentication.getPrincipal(), service, result.getSecond().get())));
        }
        logger.warn("The authentication context cannot be satisfied and the requested event {} is unrecognized", id);
        return ImmutableSet.of(new Event(this, CasWebflowConstants.TRANSITION_ID_ERROR));

    }

    public void setInitialAuthenticationAttemptWebflowEventResolver(
            final CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        this.initialAuthenticationAttemptWebflowEventResolver = initialAuthenticationAttemptWebflowEventResolver;
    }

    public void setAuthenticationContextValidator(
            final AuthenticationContextValidator authenticationContextValidator) {
        this.authenticationContextValidator = authenticationContextValidator;
    }

    private Set<Event> resumeFlow() {
        return ImmutableSet.of(new EventFactorySupport().success(this));
    }
}
