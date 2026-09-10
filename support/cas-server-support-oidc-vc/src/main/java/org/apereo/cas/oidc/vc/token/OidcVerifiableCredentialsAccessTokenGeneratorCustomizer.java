package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenGeneratorCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialsAccessTokenGeneratorCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialsAccessTokenGeneratorCustomizer implements OAuth20AccessTokenGeneratorCustomizer {
    protected final OidcVerifiableCredentialTransactionService credentialTransactionService;

    @Override
    public void customize(final AccessTokenRequestContext tokenRequestContext, final OAuth20AccessToken accessToken) {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE) {
            val preAuthorizationCode = (TransientSessionTicket) credentialTransactionService.fetchPreAuthorizationCode(
                tokenRequestContext.getPreAuthorizationCode());
            if (preAuthorizationCode != null) {
                val credentialConfigurationIds = preAuthorizationCode.getProperty("credentialConfigurationIds", List.class);
                if (credentialConfigurationIds != null && !credentialConfigurationIds.isEmpty()) {
                    accessToken.setCredentialConfigurationIds(credentialConfigurationIds);
                }
                credentialTransactionService.updatePreAuthorizationCode(preAuthorizationCode);
            }
        }
    }
}
