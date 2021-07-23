package org.apereo.cas.logout;

import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.web.support.ArgumentExtractor;
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
    private final ArgumentExtractor argumentExtractor;

    private final LogoutProperties logoutProperties;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    @Override
    public boolean supports(final RequestContext context) {
        return context != null;
    }

    @Override
    public void handle(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        Optional.ofNullable(argumentExtractor.extractService(request))
            .filter(service -> singleLogoutServiceLogoutUrlBuilder.isServiceAuthorized(service, Optional.of(request)))
            .ifPresentOrElse(service -> {
                WebUtils.putServiceIntoFlowScope(requestContext, service);
                if (logoutProperties.isFollowServiceRedirects()) {
                    LOGGER.debug("Redirecting to logout URL identified by service [{}]", service);
                    WebUtils.putLogoutRedirectUrl(requestContext, service.getOriginalUrl());
                } else {
                    LOGGER.debug("Cannot redirect to [{}] given the service is unauthorized to use CAS, "
                        + "or following logout redirects is disabled in CAS settings. "
                        + "Ensure the service is registered with CAS and is enabled to allow access", service);
                }
            }, () -> {
                val authorizedRedirectUrlFromRequest = WebUtils.getLogoutRedirectUrl(request, String.class);
                if (StringUtils.isNotBlank(authorizedRedirectUrlFromRequest)) {
                    WebUtils.putLogoutRedirectUrl(requestContext, authorizedRedirectUrlFromRequest);
                }
            });
    }
}
