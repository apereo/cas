package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0.6
 */
@Tag("X509")
public class X509SerialNumberAndIssuerDNPrincipalResolverTests {
    private static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    private X509SerialNumberAndIssuerDNPrincipalResolver resolver;

    @BeforeEach
    public void setup() {
        val context = PrincipalResolutionContext.builder()
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        resolver = new X509SerialNumberAndIssuerDNPrincipalResolver(context);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        val c = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        c.setCertificate(VALID_CERTIFICATE);
        val value = "SERIALNUMBER="
            + VALID_CERTIFICATE.getSerialNumber().toString()
            + ", " + VALID_CERTIFICATE.getIssuerDN().getName();

        assertEquals(value, this.resolver.resolve(c, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
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
