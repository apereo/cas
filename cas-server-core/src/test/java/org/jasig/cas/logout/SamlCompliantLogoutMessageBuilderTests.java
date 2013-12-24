package org.jasig.cas.logout;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SamlCompliantLogoutMessageBuilderTests {

    private final LogoutMessageBuilder builder = new SamlCompliantLogoutMessageBuilder();

    @Test
    public void testMessageBuilding() throws Exception {

        final SingleLogoutService service = mock(SingleLogoutService.class);
        final LogoutRequest request = new LogoutRequest("TICKET-ID", service);

        final String msg = builder.build(request);

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final InputStream is = new ByteArrayInputStream(msg.getBytes());
        final Document document = builder.parse(is);
        
        final NodeList list = document.getDocumentElement().getElementsByTagName("samlp:SessionIndex");
        assertEquals(list.getLength(), 1);
        
        assertEquals(list.item(0).getTextContent(), request.getTicketId());
    }
}
