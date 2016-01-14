package org.jasig.cas.web.flow.authentication;

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

    @Override
    public Event resolveInternal(final RequestContext context) {
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

                final Event event = registeredServiceAuthenticationPolicyWebflowEventResolver.resolve(context);
                if (event != null) {
                    return event;
                }
            }
            return grantTicketGrantingTicketToAuthenticationResult(context, builder, service);
        } catch (final AuthenticationException e) {
            logger.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        } catch (final Exception e) {
            logger.debug(e.getMessage(), e);
            return newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
        }
    }

}
