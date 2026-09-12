package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialAccessTokenResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class OidcVerifiableCredentialAccessTokenResponseCustomizer implements OAuth20AccessTokenResponseCustomizer {
    /**
     * OpenID4VCI 1.0 moved the proof challenge to the nonce endpoint, so no {@code c_nonce} is
     * returned here. Authorization details are echoed back because they carry the credential
     * identifiers the wallet must then present at the credential endpoint.
     */
    @Override
    public Map<String, Object> customize(final OAuth20AccessTokenResponseResult result,
                                         final Map<String, Object> model) {
        if (result.getGrantType() != OAuth20GrantTypes.PRE_AUTHORIZED_CODE
            && result.getGeneratedToken().getAccessToken().isPresent()) {
            val accessToken = result.getGeneratedToken().getAccessToken()
                .stream().map(OAuth20AccessToken.class::cast).findFirst().orElseThrow();
            if (accessToken.hasAuthorizationDetails()) {
                model.put(OAuth20Constants.AUTHORIZATION_DETAILS, accessToken.getAuthorizationDetails());
            }
        }
        return model;
    }
}
