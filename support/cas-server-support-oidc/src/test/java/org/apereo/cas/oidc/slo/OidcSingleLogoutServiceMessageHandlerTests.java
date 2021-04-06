package org.apereo.cas.oidc.slo;

import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcSingleLogoutServiceMessageHandlerTests}.
 *
 * @author Jerome LELEU
 * @since 6.2.0
 */
@Tag("OIDC")
public class OidcSingleLogoutServiceMessageHandlerTests extends AbstractOidcTests {

    private static final String LOGOUT_URL_OK = "https://mocky.io/post";

    private static final String LOGOUT_URL_BAD = "https://unknown-1234-unknown.xyz";

    @Autowired
    @Qualifier("oidcSingleLogoutServiceMessageHandler")
    private SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler;

    @Test
    public void verifyCreateLogoutRequestsFrontChannel() {
        verifyCreateLogoutRequests(RegisteredServiceLogoutType.FRONT_CHANNEL, LOGOUT_URL_OK);
    }

    @Test
    public void verifyCreateLogoutRequestsBackChannel() {
        verifyCreateLogoutRequests(RegisteredServiceLogoutType.BACK_CHANNEL, LOGOUT_URL_OK);
    }

    @Test
    public void verifyUnknownRequestsBackChannel() {
        verifyCreateLogoutRequests(RegisteredServiceLogoutType.BACK_CHANNEL, LOGOUT_URL_BAD);
    }

    @BeforeEach
    public void setup() {
        this.servicesManager.deleteAll();
    }

    private void verifyCreateLogoutRequests(final RegisteredServiceLogoutType type, final String logoutUrl) {
        val registeredService = getOidcRegisteredService("clientid", logoutUrl + ".*");
        registeredService.setLogoutType(type);
        registeredService.setLogoutUrl(logoutUrl);
        val service = RegisteredServiceTestUtils.getService(logoutUrl + "?client_id=" + registeredService.getClientId());
        servicesManager.save(registeredService);

        val executionRequest = SingleLogoutExecutionRequest.builder()
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        assertTrue(oidcSingleLogoutServiceMessageHandler.supports(executionRequest, service));
        val requests = oidcSingleLogoutServiceMessageHandler.handle(service, UUID.randomUUID().toString(), executionRequest);
        assertEquals(1, requests.size());
    }
}
