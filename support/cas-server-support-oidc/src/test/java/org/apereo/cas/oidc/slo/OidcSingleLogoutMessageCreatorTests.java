package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
class OidcSingleLogoutMessageCreatorTests extends AbstractOidcTests {

    @Autowired
    @Qualifier("oidcSingleLogoutMessageCreator")
    private SingleLogoutMessageCreator oidcSingleLogoutMessageCreator;

    @ParameterizedTest
    @EnumSource(RegisteredServiceLogoutType.class)
    void verifyBackChannelLogout(final RegisteredServiceLogoutType logoutType) throws Throwable {
        val service = getOidcRegisteredService(true, false);
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser");
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
        when(tgt.getAuthentication()).thenReturn(authentication);
        val logoutRequest = DefaultSingleLogoutRequestContext.builder()
            .logoutType(logoutType)
            .registeredService(service)
            .executionRequest(SingleLogoutExecutionRequest.builder().ticketGrantingTicket(tgt).build())
            .build();

        val message = oidcSingleLogoutMessageCreator.create(logoutRequest);
        assertNull(message.getMessage());
        val token = message.getPayload();

        val claims = JWTParser.parse(token).getJWTClaimsSet();
        assertEquals("https://sso.example.org/cas/oidc", claims.getIssuer());
        assertEquals("casuser", claims.getSubject());
        assertEquals(service.getClientId(), claims.getAudience().getFirst());
        assertNotNull(claims.getClaim("iat"));
        assertNotNull(claims.getClaim("jti"));
        assertNotNull(claims.getClaim("exp"));
        val events = (Map<String, Object>) claims.getClaim("events");
        assertNotNull(events.get("http://schemas.openid.net/event/backchannel-logout"));
        assertEquals(DigestUtils.sha(DigestUtils.sha512(TGT_ID)), claims.getClaim(OidcConstants.CLAIM_SESSION_ID));
    }
}
