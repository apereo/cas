package org.apereo.cas.support.pac4j;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.pac4j.serialization.DelegatedClientJacksonModule;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.serializers.TransientSessionTicketStringSerializer;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.oidc.profile.OidcProfile;

import java.util.Date;
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
public class DelegatedClientJacksonModuleTests {
    private static final AbstractJacksonBackedStringSerializer SERIALIZER = new TransientSessionTicketStringSerializer();

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
}
