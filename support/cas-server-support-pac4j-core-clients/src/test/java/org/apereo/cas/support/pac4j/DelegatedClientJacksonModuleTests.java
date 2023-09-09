package org.apereo.cas.support.pac4j;

import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.pac4j.serialization.DelegatedClientJacksonModule;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.serializers.TicketGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.credentials.OidcCredentials;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientJacksonModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Delegation")
class DelegatedClientJacksonModuleTests {
    private static final AbstractJacksonBackedStringSerializer SERIALIZER = new TransientSessionTicketStringSerializer();

    /**
     * @see org.pac4j.oidc.credentials.OidcCredentialsTests
     */
    private static final String ID_TOKEN = """
        eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL2p3dC1pZHAuZXhhbX
        BsZS5jb20iLCJzdWIiOiJtYWlsdG86cGVyc29uQGV4YW1wbGUuY29tIiwibmJmIjoxNDQwMTEyMDE1LCJleHAiOjE0NDAxMTU2
        MTUsImlhdCI6MTQ0MDExMjAxNSwianRpIjoiaWQxMjM0NTYiLCJ0eXAiOiJodHRwczovL2V4YW1wbGUuY29tL3JlZ2lzdGVyIn0.""";

    @Test
    @SuppressWarnings("JavaUtilDate")
    public void verifyOperation() throws Exception {
        val mapper = SERIALIZER.getObjectMapper();
        assertTrue(mapper.getRegisteredModuleIds().contains(DelegatedClientJacksonModule.class.getName()));

        val jwt = new PlainJWT(new JWTClaimsSet.Builder()
            .audience("audience")
            .subject("subject")
            .expirationTime(new Date())
            .issueTime(new Date())
            .claim("first_name", "name")
            .build());
        val oidcProfile = new OidcProfile();
        oidcProfile.setId("id");
        oidcProfile.setIdTokenString(jwt.serialize());

        val ticket = new TransientSessionTicketImpl(UUID.randomUUID().toString(),
            NeverExpiresExpirationPolicy.INSTANCE, RegisteredServiceTestUtils.getService(),
            Map.of("profiles", oidcProfile));
        val content = mapper.writeValueAsString(ticket);
        assertNotNull(mapper.readValue(content, TransientSessionTicket.class));
    }

    /**
     * This roughly reproduces an authentication as might be achived delegating to Google.
     */
    @Test
    public void verifyClientCredentials() throws Exception {
        val mapper = new TicketGrantingTicketStringSerializer().getObjectMapper();
        val creds = new OidcCredentials();
        creds.setCode(new AuthorizationCode("authcode"));
        creds.setAccessToken(new BearerAccessToken("value", 0L, Scope.parse("oidc email")));
        creds.setIdToken(JWTParser.parse(ID_TOKEN));

        val oidcProfile = new OidcProfile();
        oidcProfile.setId("id");
        oidcProfile.setIdTokenString(creds.getIdToken().serialize());
        val clientCred = new ClientCredential(creds, "client", true, oidcProfile);

        val auth = new DefaultAuthentication(
                ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneId.of("UTC")),
                null, // principal
                List.of(), // warnings
                List.of(clientCred), // credentials
                Map.of(), // attributes
                Map.of(), //successes
                Map.of()); // failures
        val ticket = new TicketGrantingTicketImpl(UUID.randomUUID().toString(),
                auth, NeverExpiresExpirationPolicy.INSTANCE);
        val content = mapper.writeValueAsString(ticket);
        assertNotNull(mapper.readValue(content, TicketGrantingTicket.class));
    }
}
