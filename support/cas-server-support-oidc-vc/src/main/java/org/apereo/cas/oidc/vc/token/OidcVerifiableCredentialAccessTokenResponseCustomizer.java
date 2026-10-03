package org.apereo.cas.oidc.vc.token;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseCustomizer;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OidcVerifiableCredentialAccessTokenResponseCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcVerifiableCredentialAccessTokenResponseCustomizer implements OAuth20AccessTokenResponseCustomizer {
    private final ObjectProvider<TicketRegistry> ticketRegistry;

    /**
     * OpenID4VCI 1.0 moved the proof challenge to the nonce endpoint, so no {@code c_nonce} is
     * returned here. Authorization details are echoed back because they carry the credential
     * identifiers the wallet must then present at the credential endpoint. A registry that encodes tickets, the
     * stateless registry, hands back an encoded ticket, so the access token is read back from it.
     *
     * @param result the access token response result
     * @param model  the response model
     * @return the response model
     */
    @Override
    public Map<String, Object> customize(final OAuth20AccessTokenResponseResult result,
                                         final Map<String, Object> model) {
        if (result.getGrantType() != OAuth20GrantTypes.PRE_AUTHORIZED_CODE
            && result.getGeneratedToken().getAccessToken().isPresent()) {
            val generatedToken = result.getGeneratedToken().getAccessToken().orElseThrow();
            val accessToken = generatedToken instanceof final OAuth20AccessToken token
                ? token
                : ticketRegistry.getObject().getTicket(generatedToken.getId(), OAuth20AccessToken.class);
            if (accessToken != null && accessToken.hasAuthorizationDetails()) {
                model.put(OAuth20Constants.AUTHORIZATION_DETAILS, accessToken.getAuthorizationDetails());
            }
        }
        return model;
    }
}
