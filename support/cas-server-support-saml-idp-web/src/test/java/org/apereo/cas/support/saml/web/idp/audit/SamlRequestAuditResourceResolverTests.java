package org.apereo.cas.support.saml.web.idp.audit;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlRequestAuditResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML2")
class SamlRequestAuditResourceResolverTests {
    @Test
    void verifyActionUnknown() {
        val resolver = new SamlRequestAuditResourceResolver();
        val result = resolver.resolveFrom(mock(JoinPoint.class), new Object());
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void verifyAction() {
        val resolver = new SamlRequestAuditResourceResolver();
        val authnRequest = mock(AuthnRequest.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(authnRequest.getIssuer()).thenReturn(issuer);
        when(authnRequest.getProtocolBinding()).thenReturn("ProtocolBinding");
        val pair = Pair.of(authnRequest, null);
        assertTrue(resolver.resolveFrom(mock(JoinPoint.class), pair).length > 0);
        assertTrue(resolver.resolveFrom(mock(JoinPoint.class), authnRequest).length > 0);
    }

    @Test
    void verifyLogout() {
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
