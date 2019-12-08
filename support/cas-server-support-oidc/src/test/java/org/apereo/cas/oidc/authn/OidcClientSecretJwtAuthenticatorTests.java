package org.apereo.cas.oidc.authn;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.EncodingUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.keys.AesKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcClientSecretJwtAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
@TestPropertySource(properties =
    "cas.authn.oauth.code.timeToKillInSeconds=60"
)
public class OidcClientSecretJwtAuthenticatorTests extends AbstractOidcTests {

    @Test
    public void verifyAction() {
        val auth = new OidcClientSecretJwtAuthenticator(servicesManager,
            registeredServiceAccessStrategyEnforcer, ticketRegistry,
            webApplicationServiceFactory, casProperties, applicationContext);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val context = new JEEContext(request, response);

        val audience = casProperties.getServer().getPrefix().concat('/'
            + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL);
        val registeredService = getOidcRegisteredService();
        val claims = getClaims(registeredService.getClientId(), registeredService.getClientId(),
            registeredService.getClientId(), audience);

        val key = EncodingUtils.generateJsonWebKey(512);
        val jwt = EncodingUtils.signJwsHMACSha512(new AesKey(key.getBytes(StandardCharsets.UTF_8)),
            claims.toJson().getBytes(StandardCharsets.UTF_8), Map.of());
        val credentials = new UsernamePasswordCredentials(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER,
            new String(jwt, StandardCharsets.UTF_8));

        val code = defaultOAuthCodeFactory.create(RegisteredServiceTestUtils.getService(),
            RegisteredServiceTestUtils.getAuthentication(),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            StringUtils.EMPTY, StringUtils.EMPTY,
            registeredService.getClientId(), new HashMap<>());
        ticketRegistry.addTicket(code);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        auth.validate(credentials, context);
        assertNotNull(credentials.getUserProfile());
    }
}
