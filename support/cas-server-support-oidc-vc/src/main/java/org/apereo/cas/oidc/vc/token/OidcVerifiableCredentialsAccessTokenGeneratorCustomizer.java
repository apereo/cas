package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.oidc.vc.offer.OidcVerifiableCredentialTransactionService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20AccessTokenGeneratorCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialsAccessTokenGeneratorCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OidcVerifiableCredentialsAccessTokenGeneratorCustomizer implements OAuth20AccessTokenGeneratorCustomizer {
    @Override
    public void customize(final AccessTokenRequestContext tokenRequestContext, final OAuth20AccessToken accessToken) {
        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.PRE_AUTHORIZED_CODE) {
            val configurationIds = tokenRequestContext.getParameters()
                .get(OidcVerifiableCredentialTransactionService.PROPERTY_CREDENTIAL_CONFIGURATION_IDS);
            if (configurationIds instanceof final List<?> values && !values.isEmpty()) {
                accessToken.setCredentialConfigurationIds(values.stream().map(Object::toString).toList());
            }
        }
    }
}
