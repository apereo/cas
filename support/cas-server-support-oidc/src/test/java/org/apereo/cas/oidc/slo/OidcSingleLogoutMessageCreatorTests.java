package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.logout.DefaultSingleLogoutRequest;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DigestUtils;

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcSingleLogoutMessageCreatorTests}.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcSingleLogoutMessageCreatorTests extends AbstractOidcTests {

    private static final String PRINCIPAL_ID = "jleleu";

    @Test
    public void verifyBackChannelLogout() throws ParseException {

        val service = getOidcRegisteredService(true, false);

        val context = OAuth20ConfigurationContext.builder()
                .idTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService)
                .casProperties(casProperties)
                .build();
        val principal = RegisteredServiceTestUtils.getPrincipal(PRINCIPAL_ID);
        var authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
        when(tgt.getAuthentication()).thenReturn(authentication);
        val logoutRequest = DefaultSingleLogoutRequest.builder()
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .registeredService(service)
                .ticketGrantingTicket(tgt)
                .build();

        val creator = new OidcSingleLogoutMessageCreator(context);
        val message = creator.create(logoutRequest);

        assertNull(message.getMessage());
        val token = message.getPayload();

        var claims = JWTParser.parse(token).getJWTClaimsSet();

        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
        assertEquals(PRINCIPAL_ID, claims.getSubject());
        assertEquals(service.getClientId(), claims.getAudience().get(0));
        assertNotNull(claims.getClaim("iat"));
        assertNotNull(claims.getClaim("jti"));
        val events = (Map<String, Object>) claims.getClaim("events");
        assertNotNull(events.get("http://schemas.openid.net/event/backchannel-logout"));
        assertEquals(DigestUtils.sha(TGT_ID), claims.getClaim(OidcConstants.CLAIM_SESSIOND_ID));
    }

    @Test
    public void verifyFrontChannelLogout() {

        val context = OAuth20ConfigurationContext.builder().build();
        val logoutRequest = DefaultSingleLogoutRequest.builder()
                .logoutType(RegisteredServiceLogoutType.FRONT_CHANNEL)
                .build();

        val creator = new OidcSingleLogoutMessageCreator(context);
        val message = creator.create(logoutRequest);

        assertEquals(StringUtils.EMPTY, message.getPayload());
        assertNull(message.getMessage());
    }
}
