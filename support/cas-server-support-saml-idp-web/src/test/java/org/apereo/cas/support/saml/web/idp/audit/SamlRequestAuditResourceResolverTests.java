package org.apereo.cas.support.saml.web.idp.audit;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRequestAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class SamlRequestAuditResourceResolverTests {
    @Test
    public void verifyAction() {
        val r = new SamlRequestAuditResourceResolver();
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn("ProtocolBinding");
        var pair = Pair.of(authnRequest, null);
        var result = r.resolveFrom(mock(JoinPoint.class), pair);
        assertNotNull(result);
        assertTrue(result.length > 0);

        val logoutRequest = mock(LogoutRequest.class);
        when(logoutRequest.getIssuer()).thenReturn(issuer);
        pair = Pair.of(authnRequest, null);
        result = r.resolveFrom(mock(JoinPoint.class), pair);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
