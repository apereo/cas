package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialAccessTokenResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialAccessTokenResponseCustomizer implements OAuth20AccessTokenResponseCustomizer {
    private final OidcVerifiableCredentialNonceService oidcVerifiableCredentialNonceService;

    @Override
    public Map<String, Object> customize(final OAuth20AccessTokenResponseResult result,
                                         final Map<String, Object> model) {
        var generateNonce = result.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE;

        if (!generateNonce && result.getGeneratedToken().getAccessToken().isPresent()) {
            val accessToken = result.getGeneratedToken().getAccessToken()
                .stream().map(OAuth20AccessToken.class::cast).findFirst().orElseThrow();
            if (accessToken.hasAuthorizationDetails()) {
                generateNonce = true;
                model.put(OAuth20Constants.AUTHORIZATION_DETAILS, accessToken.getAuthorizationDetails());
            }
        }

        if (generateNonce) {
            val nonce = oidcVerifiableCredentialNonceService.create();
            model.put(OidcConstants.C_NONCE, nonce.value());
            model.put(OidcConstants.C_NONCE_EXPIRES_IN, nonce.expiresIn());
        }

        return model;
    }
}
