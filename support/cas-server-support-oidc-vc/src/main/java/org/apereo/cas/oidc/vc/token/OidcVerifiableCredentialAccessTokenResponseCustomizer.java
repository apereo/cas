package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.nonce.OidcVerifiableCredentialNonceService;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
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
        val nonce = oidcVerifiableCredentialNonceService.create();
        model.put(OidcConstants.C_NONCE, nonce.value());
        model.put(OidcConstants.C_NONCE_EXPIRES_AT, nonce.expiresAt().truncatedTo(ChronoUnit.SECONDS));
        model.remove(OidcConstants.ID_TOKEN);
        return model;
    }
}
