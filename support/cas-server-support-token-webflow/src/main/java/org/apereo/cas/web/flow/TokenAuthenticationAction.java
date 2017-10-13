package org.apereo.cas.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.authentication.TokenCredential;
import org.apereo.cas.web.TokenRequestExtractor;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link TokenAuthenticationAction}.  This class represents an action in the webflow to retrieve
 * user information from an AES128 encrypted token. If the auth_token
 * parameter exists in the web request, it is used to create a new TokenCredential.
 *
 * @author Eric Pierce
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TokenAuthenticationAction extends AbstractNonInteractiveCredentialsAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthenticationAction.class);

    private final TokenRequestExtractor tokenRequestExtractor;
    private final ServicesManager servicesManager;

    public TokenAuthenticationAction(final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
                                     final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
                                     final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
                                     final TokenRequestExtractor tokenRequestExtractor,
                                     final ServicesManager servicesManager) {
        super(initialAuthenticationAttemptWebflowEventResolver, serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.tokenRequestExtractor = tokenRequestExtractor;
        this.servicesManager = servicesManager;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        final String authTokenValue = this.tokenRequestExtractor.extract(request);
        final Service service = WebUtils.getService(requestContext);

        if (StringUtils.isNotBlank(authTokenValue) && service != null) {
            try {
                final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

                final Credential credential = new TokenCredential(authTokenValue, service);
                LOGGER.debug("Received token authentication request [{}] ", credential);
                return credential;
            } catch (final Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }
        return null;
    }
}
