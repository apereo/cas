package org.apereo.cas.oidc.vc.authz;

import module java.base;
import org.apereo.cas.oidc.vc.services.OidcVerifiableCredentialPolicyUtils;
import org.apereo.cas.services.RegisteredService;
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
        val authzDetails = extractAuthorizationDetails(tokenRequestContext.getRegisteredService(),
            tokenRequestContext.getAuthorizationDetails());
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
                val authzDetails = extractAuthorizationDetails(registeredService, context.getAuthorizationDetails());
                return !authzDetails.isEmpty();
            }
        }
        return false;
    }

    /**
     * Authorization details are a request, not a grant. What survives here is the intersection of what
     * the wallet asked for, what the issuer publishes, and what this relying party's verifiable
     * credentials policy permits; a credential configuration the service may not obtain is dropped
     * rather than carried onto the code, so it can never reach the credential endpoint.
     *
     * @param registeredService    the registered service
     * @param authorizationDetails the requested authorization details
     * @return the authorized details, which may be empty
     */
    protected List<OidcVerifiableCredentialAuthorizationDetails> extractAuthorizationDetails(
        final RegisteredService registeredService, final String authorizationDetails) {
        val properties = configurationContext.getCasProperties().getAuthn().getOidc().getVc();
        val allowed = OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds(
            registeredService, properties.getIssuer().getCredentialConfigurations().keySet());
        return OidcVerifiableCredentialAuthorizationDetails.from(authorizationDetails, allowed);
    }
}
