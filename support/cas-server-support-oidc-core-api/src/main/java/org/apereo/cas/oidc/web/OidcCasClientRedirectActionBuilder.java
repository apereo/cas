package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcCasClientRedirectActionBuilder}, which asks CAS to act as a gateway for {@code prompt=none}, and to
 * renew the credentials for {@code prompt=login}, {@code max_age=0}, or when the single sign-on session is older than {@code max_age}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcCasClientRedirectActionBuilder extends OAuth20DefaultCasClientRedirectActionBuilder {
    private final OidcRequestSupport oidcRequestSupport;

    private final OAuth20RequestParameterResolver parameterResolver;

    public OidcCasClientRedirectActionBuilder(final String casServerLoginUrl,
                                              final OidcRequestSupport oidcRequestSupport,
                                              final OAuth20RequestParameterResolver parameterResolver) {
        super(casServerLoginUrl);
        this.oidcRequestSupport = oidcRequestSupport;
        this.parameterResolver = parameterResolver;
    }

    @Override
    public String build(final HttpServletRequest request, final String serviceUrl) {
        var renew = false;
        var gateway = false;
        val prompts = parameterResolver.resolveSupportedPromptValues(request);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            gateway = true;
        } else if (prompts.contains(OidcConstants.PROMPT_LOGIN) || OidcRequestSupport.isZeroMaxAgeAuthorizationRequest(request)
            || oidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(request)) {
            renew = true;
        }
        val url = build(serviceUrl, renew, gateway);
        LOGGER.debug("Final redirect url is [{}]", url);
        return url;
    }
}
