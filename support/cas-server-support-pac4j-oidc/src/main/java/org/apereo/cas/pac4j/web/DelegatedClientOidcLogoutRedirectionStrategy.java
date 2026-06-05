package org.apereo.cas.pac4j.web;

import module java.base;
import org.apereo.cas.logout.LogoutRedirectionResponse;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.jee.context.JEEContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.pac4j.oidc.client.OidcClient;

/**
 * This is {@link DelegatedClientOidcLogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@RequiredArgsConstructor
public class DelegatedClientOidcLogoutRedirectionStrategy implements LogoutRedirectionStrategy {
    private final ArgumentExtractor argumentExtractor;
    private final DelegatedIdentityProviders identityProviders;

    private int order;

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val webContext = new JEEContext(request, response);
        val result = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(request, DelegatedAuthenticationClientLogoutRequest.class);
        return result
            .stream()
            .map(DelegatedAuthenticationClientLogoutRequest::getClientName)
            .filter(StringUtils::isNotBlank)
            .map(clientName -> identityProviders.findClient(clientName, webContext))
            .flatMap(Optional::stream)
            .anyMatch(OidcClient.class::isInstance);
    }

    @Override
    public LogoutRedirectionResponse handle(final HttpServletRequest request,
                                            final HttpServletResponse response) throws Exception {
        val logoutRequest = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(
            request, DelegatedAuthenticationClientLogoutRequest.class).orElseThrow();
        val redirectionResponseBuilder = LogoutRedirectionResponse.builder();
        redirectionResponseBuilder.service(Optional.ofNullable(argumentExtractor.extractService(request)));
        if (StringUtils.isNotBlank(logoutRequest.getLocation()) && logoutRequest.getStatus() == HttpServletResponse.SC_FOUND) {
            redirectionResponseBuilder.logoutRedirectUrl(Optional.of(logoutRequest.getLocation()));
        }
        return redirectionResponseBuilder.build();
    }
}
