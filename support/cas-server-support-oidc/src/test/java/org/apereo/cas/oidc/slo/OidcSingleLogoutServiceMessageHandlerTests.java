package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.web.UrlValidator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcSingleLogoutServiceMessageHandlerTests}.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcSingleLogoutServiceMessageHandlerTests extends AbstractOidcTests {

    private static final String LOGOUT_URL = "http://logout";

    @Test
    public void verifyCreateLogoutRequestsFrontChannel() {

        verifyCreateLogoutRequests(RegisteredServiceLogoutType.FRONT_CHANNEL,
                LOGOUT_URL + "?iss=https%3A%2F%2Fsso.example.org%2Fcas%2Foidc&sid=" + DigestUtils.sha(TGT_ID));
    }

    @Test
    public void verifyCreateLogoutRequestsBackChannel() {

        HttpUtils.setHttpClient(mock(org.apache.http.client.HttpClient.class));

        verifyCreateLogoutRequests(RegisteredServiceLogoutType.BACK_CHANNEL, LOGOUT_URL);
    }

    private void verifyCreateLogoutRequests(final RegisteredServiceLogoutType type, final String url) {
        val context = OAuth20ConfigurationContext.builder()
                .idTokenSigningAndEncryptionService(oidcTokenSigningAndEncryptionService)
                .casProperties(casProperties)
                .build();
        val creator = new OidcSingleLogoutMessageCreator(context);
        val handler = new OidcSingleLogoutServiceMessageHandler(mock(HttpClient.class),
                creator, servicesManager, new DefaultSingleLogoutServiceLogoutUrlBuilder(mock(UrlValidator.class)),
                true, mock(AuthenticationServiceSelectionPlan.class), casProperties.getAuthn().getOidc().getIssuer());

        val singleLogoutUrl = new SingleLogoutUrl(LOGOUT_URL, type);
        val tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn(TGT_ID);
        val principal = RegisteredServiceTestUtils.getPrincipal("jleleu");
        var authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        when(tgt.getAuthentication()).thenReturn(authentication);

        val requests = handler.createLogoutRequests(TGT_ID, new SimpleWebApplicationServiceImpl(), getOidcRegisteredService(),
                Collections.singleton(singleLogoutUrl), tgt);

        assertEquals(1, requests.size());
        val request = ((List<SingleLogoutRequest>) requests).get(0);
        assertEquals(url, request.getLogoutUrl().toString());
    }
}
