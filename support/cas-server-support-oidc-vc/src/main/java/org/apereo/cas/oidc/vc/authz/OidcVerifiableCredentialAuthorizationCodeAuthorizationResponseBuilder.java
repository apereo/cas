package org.apereo.cas.oidc.vc.authz;

import module java.base;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.ticket.code.OAuth20Code;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder
    extends OAuth20AuthorizationCodeAuthorizationResponseBuilder {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    public OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder(
        final OAuth20ConfigurationContext context,
        final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder) {
        super(context, authorizationModelAndViewBuilder);
    }

    @Override
    protected OAuth20Code createOAuthCode(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val authzDetails = extractAuthorizationDetails(tokenRequestContext.getAuthorizationDetails());
        val code = super.createOAuthCode(tokenRequestContext);
        code.setAuthorizationDetails(authzDetails);
        code.setIssuerState(StringUtils.stripToNull(tokenRequestContext.getIssuerState()));
        return code;
    }

    @Override
    public boolean supports(final OAuth20AuthorizationRequest context) {
        if (super.supports(context) && StringUtils.isNotBlank(context.getAuthorizationDetails())) {
            val registeredService = Optional.ofNullable(context.getAccessTokenRequest())
                .map(AccessTokenRequestContext::getRegisteredService)
                .orElseGet(context::getRegisteredService);
            val codeChallenge = Optional.ofNullable(context.getAccessTokenRequest())
                .map(AccessTokenRequestContext::getCodeChallenge)
                .orElseGet(context::getCodeChallenge);
            if (StringUtils.isBlank(registeredService.getClientSecret()) && StringUtils.isNotBlank(codeChallenge)) {
                val authzDetails = extractAuthorizationDetails(context.getAuthorizationDetails());
                return !authzDetails.isEmpty();
            }
        }
        return false;
    }

    protected List<OidcVerifiableCredentialAuthorizationDetails> extractAuthorizationDetails(
        final String authorizationDetails) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        return OidcVerifiableCredentialAuthorizationDetails.from(authorizationDetails,
            properties.getIssuer().getCredentialConfigurations().keySet());

    }
}
