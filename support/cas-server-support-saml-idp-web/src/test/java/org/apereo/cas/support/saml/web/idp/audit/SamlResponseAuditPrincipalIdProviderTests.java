package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlResponseAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class SamlResponseAuditPrincipalIdProviderTests {
    @Test
    public void verifyAction() {
        val r = new SamlResponseAuditPrincipalIdProvider();
        val response = mock(Response.class);
        val issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getDestination()).thenReturn("https://sp.example.org");

        val assertion = mock(Assertion.class);
        val subject = mock(Subject.class);
        val nameId = mock(NameID.class);
        when(nameId.getValue()).thenReturn("casuser");
        when(subject.getNameID()).thenReturn(nameId);
        when(assertion.getSubject()).thenReturn(subject);
        when(response.getAssertions()).thenReturn(CollectionUtils.wrapList(assertion));
        val result = r.getPrincipalIdFrom(CoreAuthenticationTestUtils.getAuthentication(), response, null);
        assertNotNull(result);
        assertEquals("casuser", result);
        assertTrue(r.supports(CoreAuthenticationTestUtils.getAuthentication(), response, null));
    }
}
