package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.AbstractOAuth20Tests;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultTokenGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
@TestPropertySource(properties = {
    "cas.authn.oauth.access-token.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
    "cas.authn.oauth.access-token.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ",
    "cas.authn.oauth.access-token.crypto.enabled=true"
})
public class OAuth20DefaultTokenGeneratorTests extends AbstractOAuth20Tests {

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyAccessTokenAsJwt() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val decoded = this.oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);

        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);

        val ticketId = jwt.getJwtId();
        assertNotNull(ticketId);
        assertNotNull(this.ticketRegistry.getTicket(ticketId, OAuth20AccessToken.class));
    }

    @Test
    public void verifyAccessTokenIsRefreshed() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        val authentication = RegisteredServiceTestUtils.getAuthentication("casuser");

        ModelAndView mv = generateAccessTokenResponseAndGetModelAndView(
                registeredService,
                authentication,
                OAuth20GrantTypes.AUTHORIZATION_CODE
        );
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val decoded = this.oauthAccessTokenJwtCipherExecutor.decode(at).toString();
        assertNotNull(decoded);
        val jwt = JwtClaims.parse(decoded);
        assertNotNull(jwt);
        assertNotNull(jwt.getIssuedAt());
        assertNotEquals(authentication.getAuthenticationDate().toEpochSecond(), jwt.getIssuedAt().getValue());
        assertNotNull(jwt.getExpirationTime());

        mv = generateAccessTokenResponseAndGetModelAndView(
                registeredService,
                authentication,
                OAuth20GrantTypes.REFRESH_TOKEN
        );
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));
        val refreshedAt = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val refreshedDecoded = this.oauthAccessTokenJwtCipherExecutor.decode(refreshedAt).toString();
        assertNotNull(refreshedDecoded);
        val refreshedJwt = JwtClaims.parse(refreshedDecoded);
        assertNotNull(refreshedJwt);
        assertNotNull(refreshedJwt.getIssuedAt());
        assertNotEquals(authentication.getAuthenticationDate().toEpochSecond(), refreshedJwt.getIssuedAt().getValue());
        assertNotNull(refreshedJwt.getExpirationTime());
        assertNotEquals(jwt.getExpirationTime().getValue(), refreshedJwt.getExpirationTime().getValue());
    }
}
