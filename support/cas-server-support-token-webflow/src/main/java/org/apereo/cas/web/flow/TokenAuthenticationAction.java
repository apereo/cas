package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.authentication.TokenCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.TokenRequestExtractor;
import org.apereo.cas.web.flow.actions.AbstractNonInteractiveCredentialsAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link TokenAuthenticationAction}.  This class represents an action in the webflow to retrieve
 * user information from an AES128 encrypted token. If the auth_token
 * parameter exists in the web request, it is used to create a new TokenCredential.
 *
 * @author Eric Pierce
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class TokenAuthenticationAction extends AbstractNonInteractiveCredentialsAction {
    private final TokenRequestExtractor tokenRequestExtractor;
    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    private final CasConfigurationProperties casProperties;
    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    public TokenAuthenticationAction(
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        final TokenRequestExtractor tokenRequestExtractor,
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
        final CasConfigurationProperties casProperties) {
        super(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy);
        this.tokenRequestExtractor = tokenRequestExtractor;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
        this.casProperties = casProperties;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val service = resolveServiceFromRequest(requestContext);
        return Optional.ofNullable(tokenRequestExtractor.extract(request))
            .or(() -> {
                val tokenValue = service.getAttributes().get(TokenConstants.PARAMETER_NAME_TOKEN);
                return CollectionUtils.firstElement(tokenValue).map(Object::toString);
            })
            .filter(StringUtils::isNotBlank)
            .map(authTokenValue -> {
                val registeredService = servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
                val credential = new TokenCredential(authTokenValue, service);
                LOGGER.debug("Received token authentication request [{}] ", credential);
                return credential;
            })
            .orElse(null);
    }

    protected Service resolveServiceFromRequest(final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> {
            val givenService = Optional.ofNullable(WebUtils.getService(requestContext))
                .orElseGet(() -> webApplicationServiceFactory.createService(casProperties.getServer().getPrefix()));
            return serviceSelectionStrategy.resolveService(givenService);
        });
    }
}
