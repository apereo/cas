package org.apereo.cas.web.flow.logout;

import module java.base;
import org.apereo.cas.logout.LogoutRedirectionResponse;
import org.apereo.cas.logout.LogoutRedirectionStrategy;
import org.apereo.cas.support.pac4j.authentication.DelegatedAuthenticationClientLogoutRequest;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DelegatedAuthenticationLogoutRedirectionStrtategy}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@RequiredArgsConstructor
public class DelegatedAuthenticationLogoutRedirectionStrtategy implements LogoutRedirectionStrategy {
    private final ArgumentExtractor argumentExtractor;

    private int order;

    @Override
    public boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        val result = DelegationWebflowUtils.getDelegatedAuthenticationLogoutRequest(request, DelegatedAuthenticationClientLogoutRequest.class);
        return result.isPresent();
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
