package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
    private Authenticator authenticator;

    @Test
    void verifyNoToken() {
        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", "ABCD123");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, "CODE-UNKNOWN");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(InvalidTicketException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }


    @Test
    void verifyAuthenticationPlainWithoutSecret() throws Throwable {
        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", "ABCD123");
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(new OAuth20DefaultCode(id,
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), "ABCD123",
            "plain", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, id);
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("clientWithoutSecret", credentials.getUserProfile().getId());
    }

    @Test
    void verifyAuthenticationPlainWithSecretTransmittedByFormAuthn() throws Throwable {
        val credentials = new UsernamePasswordCredentials("client", "ABCD123");
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(
            new OAuth20DefaultCode(id, RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(10),
                new MockTicketGrantingTicket("casuser"),
                new ArrayList<>(), "ABCD123",
                "plain", "clientid12345", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, id);
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    void verifyAuthenticationPlainWithSecretTransmittedByBasicAuthn() throws Throwable {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val id = "CODE-%s".formatted(RandomUtils.randomAlphabetic(12));
        ticketRegistry.addTicket(
            new OAuth20DefaultCode(id, RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(10),
                new MockTicketGrantingTicket("casuser"),
                new ArrayList<>(), "ABCD123",
                "plain", "clientid12345", new HashMap<>(),
                OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("client:secret"));
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, id);
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    void verifyAuthenticationHashedWithoutSecret() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", "ABCD123");
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("clientWithoutSecret", credentials.getUserProfile().getId());
    }

    @Test
    void verifyUnknownDigest() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val credentials = new UsernamePasswordCredentials("clientWithoutSecret", "ABCD123");
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "unknown", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, "clientWithoutSecret");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }

    @Test
    void verifyAuthenticationHashedWithSecretTransmittedByFormAuthn() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val credentials = new UsernamePasswordCredentials("client", "ABCD123");
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    void verifyAuthenticationHashedWithSecretTransmittedByBasicFormAuthn() throws Throwable {
        val hash = EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256("ABCD123"));
        val credentials = new UsernamePasswordCredentials("client", "ABCD123");
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-1234567890",
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(), hash, "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + EncodingUtils.encodeBase64("client:secret"));
        request.addParameter(OAuth20Constants.CODE_VERIFIER, "ABCD123");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    void verifyAuthenticationNotHashedCorrectly() throws Throwable {
        servicesManager.save(service);
        
        val credentials = new UsernamePasswordCredentials("client", "ABCD123");
        val request = new MockHttpServletRequest();
        val ticket = new OAuth20DefaultCode("CODE-%s".formatted(RandomUtils.randomAlphabetic(12)),
            RegisteredServiceTestUtils.getService(), RegisteredServiceTestUtils.getAuthentication(),
            new HardTimeoutExpirationPolicy(10),
            new MockTicketGrantingTicket("casuser"),
            new ArrayList<>(),
            "something-else", "s256", "clientid12345", new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        ticketRegistry.addTicket(ticket);
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.CODE_VERIFIER, RandomUtils.randomAlphabetic(12));
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "secret");
        request.addParameter(OAuth20Constants.CODE, ticket.getId());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        assertThrows(CredentialsException.class,
            () -> authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials));
    }
}
