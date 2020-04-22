package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.val;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

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
    "cas.authn.oauth.access-token.crypto.enabled=true",
    "cas.authn.oauth.device-token.refresh-interval=PT1S"
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
    public void verifySlowDown() {
        val generator = new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory, defaultDeviceTokenFactory,
            oAuthRefreshTokenFactory, centralAuthenticationService, casProperties);
        val token = defaultDeviceTokenFactory.createDeviceCode(
            RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceTokenFactory.createDeviceUserCode(token);
        ticketRegistry.addTicket(userCode);
        val holder = AccessTokenRequestDataHolder.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .build();
        assertThrows(ThrottledOAuth20DeviceUserCodeApprovalException.class, () -> generator.generate(holder));
    }

    @Test
    public void verifyUnapproved() throws Exception {
        val generator = new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory, defaultDeviceTokenFactory,
            oAuthRefreshTokenFactory, centralAuthenticationService, casProperties);
        val token = defaultDeviceTokenFactory.createDeviceCode(
            RegisteredServiceTestUtils.getService("https://device.oauth.org"));
        ticketRegistry.addTicket(token);
        val userCode = defaultDeviceTokenFactory.createDeviceUserCode(token);
        ticketRegistry.addTicket(userCode);

        Thread.sleep(2000);
        val holder = AccessTokenRequestDataHolder.builder()
            .responseType(OAuth20ResponseTypes.DEVICE_CODE)
            .deviceCode(token.getId())
            .build();
        assertThrows(UnapprovedOAuth20DeviceUserCodeException.class, () -> generator.generate(holder));
    }
}
