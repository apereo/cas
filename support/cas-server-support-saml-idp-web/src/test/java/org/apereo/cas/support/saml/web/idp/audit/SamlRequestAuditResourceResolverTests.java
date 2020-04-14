package org.apereo.cas.support.saml.web.idp.audit;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRequestAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@Tag("SAML")
public class SamlRequestAuditResourceResolverTests {
    @Test
    public void verifyActionUnknown() {
        val r = new SamlRequestAuditResourceResolver();
        val result = r.resolveFrom(mock(JoinPoint.class), new Object());
        assertNotNull(result);
        assertTrue(result.length == 0);
    }


    @Test
    public void verifyAction() {
        val r = new SamlRequestAuditResourceResolver();
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn("ProtocolBinding");
        val pair = Pair.of(authnRequest, null);
        val result = r.resolveFrom(mock(JoinPoint.class), pair);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    public void verifyLogout() {
        val r = new SamlRequestAuditResourceResolver();
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");

        val logoutRequest = mock(LogoutRequest.class);
        when(logoutRequest.getIssuer()).thenReturn(issuer);
        val pair = Pair.of(logoutRequest, null);
        val result = r.resolveFrom(mock(JoinPoint.class), pair);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
