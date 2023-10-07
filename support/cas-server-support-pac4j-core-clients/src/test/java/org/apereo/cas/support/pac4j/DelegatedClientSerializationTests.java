package org.apereo.cas.support.pac4j;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.serializers.TicketGrantingTicketStringSerializer;
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.oidc.credentials.OidcCredentials;
import org.pac4j.oidc.profile.OidcProfile;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientSerializationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Delegation")
@SuppressWarnings("JavaUtilDate")
class DelegatedClientSerializationTests {
    private static final AbstractJacksonBackedStringSerializer TST_SERIALIZER = new TransientSessionTicketStringSerializer();
    private static final AbstractJacksonBackedStringSerializer TGT_SERIALIZER = new TicketGrantingTicketStringSerializer();

    private static final String ID_TOKEN = """
        eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpc3MiOiJodHRwczovL2p3dC1pZHAuZXhhbX
        BsZS5jb20iLCJzdWIiOiJtYWlsdG86cGVyc29uQGV4YW1wbGUuY29tIiwibmJmIjoxNDQwMTEyMDE1LCJleHAiOjE0NDAxMTU2
        MTUsImlhdCI6MTQ0MDExMjAxNSwianRpIjoiaWQxMjM0NTYiLCJ0eXAiOiJodHRwczovL2V4YW1wbGUuY29tL3JlZ2lzdGVyIn0.""";

    @Test
    public void verifyOperation() throws Exception {
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
        val content = TST_SERIALIZER.toString(ticket);
        assertSame(TST_SERIALIZER.getTypeToSerialize(), TST_SERIALIZER.from(content).getClass());
    }

    @Test
    public void verifyClientCredentials() throws Throwable {
        val creds = new OidcCredentials();
        creds.setCode("authcode");
        creds.setAccessToken(new BearerAccessToken("value", 0L, Scope.parse("oidc email")).toJSONObject());
        creds.setIdToken(ID_TOKEN);

        val oidcProfile = new OidcProfile();
        oidcProfile.setId("id");
        oidcProfile.setIdTokenString(creds.getIdToken());
        val clientCred = new ClientCredential(creds, "client", true, oidcProfile);

        val auth = DefaultAuthenticationBuilder
            .newInstance(RegisteredServiceTestUtils.getPrincipal())
            .addCredential(clientCred)
            .build();
        val ticket = new TicketGrantingTicketImpl(
            UUID.randomUUID().toString(),
            auth, NeverExpiresExpirationPolicy.INSTANCE);
        val content = TGT_SERIALIZER.toString(ticket);
        assertSame(TGT_SERIALIZER.getTypeToSerialize(), TGT_SERIALIZER.from(content).getClass());
    }
}
