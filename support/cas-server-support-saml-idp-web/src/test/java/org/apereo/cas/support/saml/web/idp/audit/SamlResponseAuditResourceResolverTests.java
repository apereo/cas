package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.util.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.soap.soap11.Fault;
import org.opensaml.soap.soap11.FaultActor;
import org.opensaml.soap.soap11.FaultString;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlResponseAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class SamlResponseAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        final var r = new SamlResponseAuditResourceResolver();
        final var response = mock(Response.class);
        final var issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getDestination()).thenReturn("https://sp.example.org");

        var result = r.resolveFrom(mock(JoinPoint.class), response);
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        final var envelope = mock(Envelope.class);
        final var body = mock(Body.class);

        when(body.getUnknownXMLObjects()).thenReturn(CollectionUtils.wrapList(response));
        when(envelope.getBody()).thenReturn(body);
        result = r.resolveFrom(mock(JoinPoint.class), envelope);
        assertNotNull(result);
        assertTrue(result.length > 0);

        final var fault = mock(Fault.class);
        final var actor = mock(FaultActor.class);
        when(actor.getValue()).thenReturn("actor");
        final var msg = mock(FaultString.class);
        when(msg.getValue()).thenReturn("message");
        when(fault.getMessage()).thenReturn(msg);
        when(fault.getActor()).thenReturn(actor);
        when(body.getUnknownXMLObjects()).thenReturn(CollectionUtils.wrapList(fault));
        result = r.resolveFrom(mock(JoinPoint.class), envelope);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
