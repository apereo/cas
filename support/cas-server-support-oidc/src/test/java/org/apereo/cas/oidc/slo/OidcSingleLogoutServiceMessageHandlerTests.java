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

    private static final String LOGOUT_URL = "https://mocky.io/post";

    @Autowired
    @Qualifier("oidcSingleLogoutServiceMessageHandler")
    private SingleLogoutServiceMessageHandler oidcSingleLogoutServiceMessageHandler;

    @Test
    public void verifyCreateLogoutRequestsFrontChannel() {
        verifyCreateLogoutRequests(RegisteredServiceLogoutType.FRONT_CHANNEL);
    }

    @Test
    public void verifyCreateLogoutRequestsBackChannel() {
        verifyCreateLogoutRequests(RegisteredServiceLogoutType.BACK_CHANNEL);
    }

    @BeforeEach
    public void setup() {
        this.servicesManager.deleteAll();
    }

    private void verifyCreateLogoutRequests(final RegisteredServiceLogoutType type) {
        val registeredService = getOidcRegisteredService("clientid", LOGOUT_URL + ".*");
        registeredService.setLogoutType(type);
        registeredService.setLogoutUrl(LOGOUT_URL);
        val service = RegisteredServiceTestUtils.getService(LOGOUT_URL + "?client_id=" + registeredService.getClientId());
        servicesManager.save(registeredService);

        val executionRequest = SingleLogoutExecutionRequest.builder()
            .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
            .build();
        assertTrue(oidcSingleLogoutServiceMessageHandler.supports(executionRequest, service));
        val requests = oidcSingleLogoutServiceMessageHandler.handle(service, UUID.randomUUID().toString(), executionRequest);
        assertEquals(1, requests.size());
    }
}
