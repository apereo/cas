package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0
 */
@Tag("X509")
public class X509SubjectDNPrincipalResolverTests {
    private static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    private X509SubjectDNPrincipalResolver resolver;

    private X509SubjectDNPrincipalResolver resolverRFC2253;

    @BeforeEach
    public void setup() {
        val context = PrincipalResolutionContext.builder()
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        resolver = new X509SubjectDNPrincipalResolver(context, null);
        resolverRFC2253 = new X509SubjectDNPrincipalResolver(context, X500Principal.RFC2253);
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        assertEquals(VALID_CERTIFICATE.getSubjectDN().getName(), this.resolver.resolve(c,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler())).getId());
    }

    @Test
    public void verifyResolvePrincipalInternalRFC2253() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        assertEquals(VALID_CERTIFICATE.getSubjectX500Principal().getName(X500Principal.RFC2253), this.resolverRFC2253.resolve(c,
            Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
            Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler())).getId());
    }


    @Test
    public void verifySupport() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(c));
    }

    @Test
    public void verifySupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }
}
