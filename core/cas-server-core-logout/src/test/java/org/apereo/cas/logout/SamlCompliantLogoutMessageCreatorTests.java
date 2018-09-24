package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class SamlCompliantLogoutMessageCreatorTests {
    public static final String CONST_TEST_URL = "https://google.com";

    private final LogoutMessageCreator builder = new SamlCompliantLogoutMessageCreator();

    @Test
    public void verifyMessageBuilding() throws Exception {

        val service = mock(WebApplicationService.class);
        when(service.getOriginalUrl()).thenReturn(CONST_TEST_URL);
        val logoutUrl = new URL(service.getOriginalUrl());
        val request = new DefaultLogoutRequest("TICKET-ID", service, logoutUrl, mock(RegisteredService.class),
            new MockTicketGrantingTicket("casuser"));

        val msg = builder.create(request);

        val factory = DocumentBuilderFactory.newInstance();
        val documentBuilder = factory.newDocumentBuilder();

        val is = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
        val document = documentBuilder.parse(is);

        val list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
        assertEquals(1, list.getLength());

        assertEquals(list.item(0).getTextContent(), request.getTicketId());
    }
}
