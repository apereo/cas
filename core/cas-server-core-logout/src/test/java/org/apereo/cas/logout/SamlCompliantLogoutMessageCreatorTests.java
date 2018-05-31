package org.apereo.cas.logout;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class SamlCompliantLogoutMessageCreatorTests {
    public static final String CONST_TEST_URL = "https://google.com";

    private final LogoutMessageCreator builder = new SamlCompliantLogoutMessageCreator();

    @Test
    public void verifyMessageBuilding() throws Exception {

        final var service = mock(WebApplicationService.class);
        when(service.getOriginalUrl()).thenReturn(CONST_TEST_URL);
        final var logoutUrl = new URL(service.getOriginalUrl());
        final var request = new DefaultLogoutRequest("TICKET-ID", service, logoutUrl);

        final var msg = builder.create(request);

        final var factory = DocumentBuilderFactory.newInstance();
        final var builder = factory.newDocumentBuilder();

        final InputStream is = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8));
        final var document = builder.parse(is);
        
        final var list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
        assertEquals(1, list.getLength());
        
        assertEquals(list.item(0).getTextContent(), request.getTicketId());
    }
}
