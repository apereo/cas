package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DefaultLogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultLogoutRedirectionStrategy implements LogoutRedirectionStrategy {
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    private final LogoutProperties logoutProperties;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    @Override
    public boolean supports(final RequestContext context) {
        return context != null;
    }

    @Override
    public void handle(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        val paramName = logoutProperties.getRedirectParameter();
        LOGGER.trace("Using parameter name [{}] to detect destination service, if any", paramName);
        val service = requestContext.getRequestParameters().get(paramName);
        LOGGER.trace("Located target service [{}] for redirection after logout", service);

        val authorizedRedirectUrlFromRequest = WebUtils.getLogoutRedirectUrl(request, String.class);
        if (StringUtils.isNotBlank(service) && logoutProperties.isFollowServiceRedirects()) {
            val webAppService = webApplicationServiceFactory.createService(service);
            if (singleLogoutServiceLogoutUrlBuilder.isServiceAuthorized(webAppService, Optional.of(request))) {
                LOGGER.debug("Redirecting to logout URL [{}]", service);
                WebUtils.putLogoutRedirectUrl(requestContext, service);
            } else {
                LOGGER.warn("Cannot redirect to [{}] given the service is unauthorized to use CAS. "
                    + "Ensure the service is registered with CAS and is enabled to allow access", service);
            }
        } else if (StringUtils.isNotBlank(authorizedRedirectUrlFromRequest)) {
            WebUtils.putLogoutRedirectUrl(requestContext, authorizedRedirectUrlFromRequest);
        } else {
            LOGGER.debug("No target service is located for redirection after logout, or following service redirects is disabled");
        }
    }
}
