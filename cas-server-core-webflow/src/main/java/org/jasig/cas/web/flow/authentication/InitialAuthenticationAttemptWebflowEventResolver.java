package org.jasig.cas.web.flow.authentication;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategySupport;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.xml.transform.URIResolver;
import java.util.Set;

/**
 * This is {@link InitialAuthenticationAttemptWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("initialAuthenticationAttemptWebflowEventResolver")
public class InitialAuthenticationAttemptWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    @Qualifier("registeredServiceAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver registeredServiceAuthenticationPolicyWebflowEventResolver;

    @Autowired
    @Qualifier("principalAttributeAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver principalAttributeAuthenticationPolicyWebflowEventResolver;

    @Autowired
    @Qualifier("registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver")
    private CasWebflowEventResolver registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver;

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        try {

            final Credential credential = getCredentialFromContext(context);
            final AuthenticationResultBuilder builder =
                    this.authenticationSystemSupport.handleInitialAuthenticationTransaction(credential);

            WebUtils.putAuthenticationResultBuilder(builder, context);
            WebUtils.putAuthentication(builder.getInitialAuthentication(), context);

            final Service service = WebUtils.getService(context);
            if (service != null) {
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategySupport.ensureServiceAccessIsAllowed(service, registeredService);

                final Set<Event> roleEvents = registeredServicePrincipalAttributeAuthenticationPolicyWebflowEventResolver.resolve(context);
                final Set<Event> attributeEvents = principalAttributeAuthenticationPolicyWebflowEventResolver.resolve(context);
                final Event event = registeredServiceAuthenticationPolicyWebflowEventResolver.resolveSingle(context);

                final ImmutableSet.Builder<Event> eventBuilder = ImmutableSet.builder();
                if (event != null) {
                    eventBuilder.add(event);
                }
                if (roleEvents != null) {
                    eventBuilder.addAll(roleEvents);
                }
                if (attributeEvents != null) {
                    eventBuilder.addAll(attributeEvents);
                }
                final Set<Event> resolvedEvents = eventBuilder.build();


            }
            return ImmutableSet.of(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            Event event = returnAuthenticationExceptionEventIfNeeded(e);
            if (event == null) {
                logger.debug(e.getMessage(), e);
                event = newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
            }
            return ImmutableSet.of(event);
        }
    }

    private Event returnAuthenticationExceptionEventIfNeeded(final Exception e) {

        final AuthenticationException ex;
        if (e instanceof AuthenticationException) {
            ex = (AuthenticationException) e;
        } else if (e.getCause() instanceof AuthenticationException) {
            ex = (AuthenticationException) e.getCause();
        } else {
            return null;
        }

        logger.debug(ex.getMessage(), ex);
        return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, ex);
    }

}
