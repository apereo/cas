package org.apereo.cas.logout;

import module java.base;
import module java.xml;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.SingleLogoutMessageCreator;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Tag("SAMLLogout")
class SamlCompliantLogoutMessageCreatorTests {
    private static final String TEST_URL = "https://google.com";

    private final SingleLogoutMessageCreator builder = new DefaultSingleLogoutMessageCreator();

    @Test
    void verifyMessageBuilding() throws Throwable {

        val service = RegisteredServiceTestUtils.getService(TEST_URL);
        val logoutUrl = new URI(service.getOriginalUrl()).toURL();
        val request = DefaultSingleLogoutRequestContext.builder()
            .ticketId("TICKET-ID")
            .service(service)
            .logoutUrl(logoutUrl)
            .registeredService(mock(RegisteredService.class))
            .executionRequest(SingleLogoutExecutionRequest.builder()
                .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                .build())
            .build();

        val msg = builder.create(request);

        val factory = DocumentBuilderFactory.newInstance();
        val documentBuilder = factory.newDocumentBuilder();

        try (val is = new ByteArrayInputStream(msg.getPayload().getBytes(StandardCharsets.UTF_8))) {
            val document = documentBuilder.parse(is);
            val list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
            assertEquals(1, list.getLength());
            assertEquals(list.item(0).getTextContent(), request.getTicketId());
        }
    }
}
