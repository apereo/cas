package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.OAuth20TestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredServiceClientSecret;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ProofKeyCodeExchangeAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
class OAuth20ProofKeyCodeExchangeAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    @Autowired
    @Qualifier("oauthProofKeyCodeExchangeAuthenticator")
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @BeforeEach
    @Override
    void initialize() {
        service = new OAuthRegisteredService();
        service.setId(RandomUtils.nextLong());
        service.setName("PKCE-" + UUID.randomUUID());
        service.setServiceId("https://" + UUID.randomUUID() + ".example.org");
        service.setClientId(UUID.randomUUID().toString());
        service.setClientSecrets(new ArrayList<>(List.of(OAuthRegisteredServiceClientSecret.withoutExpiration("secret"))));

        serviceWithoutSecret = new OAuthRegisteredService();
        serviceWithoutSecret.setId(RandomUtils.nextLong());
        serviceWithoutSecret.setName("PKCE-" + UUID.randomUUID());
        serviceWithoutSecret.setServiceId("https://" + UUID.randomUUID() + ".example.org");
        serviceWithoutSecret.setClientId(UUID.randomUUID().toString());
        servicesManager.save(service, serviceWithoutSecret);
    }

    @Test
    void verifyNoToken() {
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, "CODE-UNKNOWN");
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), "ABCD123")));
    }


    @Test
    void verifyAuthenticationPlainWithoutSecret() throws Throwable {
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(new OAuth20DefaultCode(id,
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), "ABCD123",
            "plain", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, id);
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), "ABCD123"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(serviceWithoutSecret.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyAuthenticationPlainWithSecretTransmittedByFormAuthn() throws Throwable {
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(
            new OAuth20DefaultCode(id, RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(10),
                new MockTicketGrantingTicket("casuser"),
                new ArrayList<>(), "ABCD123",
                "plain", "clientid12345", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, id);
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), "ABCD123"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(service.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyAuthenticationPlainWithSecretTransmittedByBasicAuthn() throws Throwable {
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(
            new OAuth20DefaultCode(id, RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(10),
                new MockTicketGrantingTicket("casuser"),
                new ArrayList<>(), "ABCD123",
                "plain", "clientid12345", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(service.getClientId() + ":secret"));
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, id);
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), "secret"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(service.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyAuthenticationHashedWithoutSecret() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), "ABCD123"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(serviceWithoutSecret.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyUnknownDigest() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "unknown", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, serviceWithoutSecret.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, serviceWithoutSecret.getClientId(), "ABCD123")));
    }

    @Test
    void verifyAuthenticationHashedWithSecretTransmittedByFormAuthn() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), "ABCD123"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(service.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyAuthenticationHashedWithSecretTransmittedByBasicFormAuthn() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64(service.getClientId() + ":secret"));
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val credentialsProfile = authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), "ABCD123"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(service.getClientId(), credentialsProfile.orElseThrow().getId());
    }

    @Test
    void verifyAuthenticationNotHashedCorrectly() throws Throwable {
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            "something-else", "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CODE_VERIFIER, RandomUtils.randomAlphabetic(12));
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        assertThrows(BadCredentialsException.class,
            () -> authenticator.authenticate(OAuth20TestUtils.newClientAuthenticationToken(request, service.getClientId(), "ABCD123")));
    }
}
