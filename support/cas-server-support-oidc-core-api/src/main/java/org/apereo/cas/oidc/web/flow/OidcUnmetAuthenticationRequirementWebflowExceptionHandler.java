package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcUnmetAuthenticationRequirementWebflowExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class OidcUnmetAuthenticationRequirementWebflowExceptionHandler implements CasWebflowExceptionHandler<AuthenticationException> {
    private final OidcConfigurationContext context;

    private int order;

    @Override
    public Event handle(final AuthenticationException exception, final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val service = context.getArgumentExtractor().extractService(request);
            val redirectUri = CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.REDIRECT_URI)).orElseThrow().toString();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByRedirectUri(context.getServicesManager(), redirectUri);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            val url = new URIBuilder(redirectUri).addParameter(OAuth20Constants.ERROR, OAuth20Constants.UNMET_AUTHENTICATION_REQUIREMENTS).build();
            requestContext.getRequestScope().put("url", url);
            return EVENT_FACTORY.event(this, CasWebflowConstants.TRANSITION_ID_REDIRECT);
        });
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) {
        return exception instanceof final AuthenticationException e
               && e.getHandlerErrors().containsKey(MultifactorAuthenticationProviderAbsentException.class.getSimpleName());
    }
}
