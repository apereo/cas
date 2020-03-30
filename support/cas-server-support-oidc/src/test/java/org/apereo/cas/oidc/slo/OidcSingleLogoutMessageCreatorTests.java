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

import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import lombok.val;

import java.util.Map;
import java.util.Optional;

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

    private static final String ISSUER = "https://mycasserver";
    private static final String TGT_ID = "TGT-0";
    private static final String PRINCIPAL_ID = "jleleu";

    @Test
    public void verifyBackChannelLogout() throws MalformedClaimException {

        casProperties.getAuthn().getOidc().setIssuer(ISSUER);
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
                .registeredService(getOidcRegisteredService())
                .ticketGrantingTicket(tgt)
                .build();

        val creator = new OidcSingleLogoutMessageCreator(context);
        val message = creator.create(logoutRequest);

        assertNull(message.getMessage());
        val token = message.getPayload();
        val claims = oidcTokenSigningAndEncryptionService.decode(token, Optional.of(getOidcRegisteredService()));

        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(PRINCIPAL_ID, claims.getSubject());
        assertEquals(getOidcRegisteredService().getClientId(), claims.getAudience());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getJwtId());
        val events = (Map<String, Object>) claims.getClaimValue("events");
        assertNotNull(events.get("http://schemas.openid.net/event/backchannel-logout"));
        assertEquals(DigestUtils.sha(TGT_ID), claims.getClaimValue(OidcConstants.CLAIM_SESSIOND_ID));
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
