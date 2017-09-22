package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class SamlCompliantLogoutMessageCreatorTests {

    private final LogoutMessageCreator builder = new SamlCompliantLogoutMessageCreator();

    @Test
    public void verifyMessageBuilding() throws Exception {

        final WebApplicationService service = mock(WebApplicationService.class);
        when(service.getOriginalUrl()).thenReturn(RegisteredServiceTestUtils.CONST_TEST_URL);
        final URL logoutUrl = new URL(service.getOriginalUrl());
        final DefaultLogoutRequest request = new DefaultLogoutRequest("TICKET-ID", service, logoutUrl);

        final String msg = builder.create(request);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final InputStream is = new ByteArrayInputStream(msg.getBytes());
        final Document document = builder.parse(is);
        
        final NodeList list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
        assertEquals(list.getLength(), 1);
        
        assertEquals(list.item(0).getTextContent(), request.getTicketId());
    }
}
