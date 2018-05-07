package org.apereo.cas.support.saml.web.idp.audit;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlResponseAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class SamlResponseAuditPrincipalIdProviderTests {
    @Test
    public void verifyAction() {
        final var r = new SamlResponseAuditPrincipalIdProvider();
        final var response = mock(Response.class);
        final var issuer = mock(Issuer.class);
        when(issuer.getValue()).thenReturn("https://idp.example.org");
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getDestination()).thenReturn("https://sp.example.org");

        final var assertion = mock(Assertion.class);
        final var subject = mock(Subject.class);
        final var nameId = mock(NameID.class);
        when(nameId.getValue()).thenReturn("casuser");
        when(subject.getNameID()).thenReturn(nameId);
        when(assertion.getSubject()).thenReturn(subject);
        when(response.getAssertions()).thenReturn(CollectionUtils.wrapList(assertion));
        final var result = r.getPrincipalIdFrom(CoreAuthenticationTestUtils.getAuthentication(), response, null);
        assertNotNull(result);
        assertEquals("casuser", result);
        assertTrue(r.supports(CoreAuthenticationTestUtils.getAuthentication(), response, null));
    }
}
