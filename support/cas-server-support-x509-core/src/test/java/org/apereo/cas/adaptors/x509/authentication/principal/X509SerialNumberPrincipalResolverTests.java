package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Scott Battaglia
 * @author Jan Van der Velpen
 * @since 3.0.0.6
 */
@Tag("X509")
public class X509SerialNumberPrincipalResolverTests {
    private static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    private X509SerialNumberPrincipalResolver resolver;

    private PrincipalResolutionContext resolutionContext;

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        resolutionContext = PrincipalResolutionContext.builder()
            .servicesManager(servicesManager)
            .attributeDefinitionStore(attributeDefinitionStore)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(IPersonAttributeDao.WILDCARD))
            .build();
        resolver = new X509SerialNumberPrincipalResolver(resolutionContext);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
    }

    @Test
    public void verifyResolvePrincipalInternal() {
        val credential = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        credential.setCertificate(VALID_CERTIFICATE);
        assertEquals(VALID_CERTIFICATE.getSerialNumber().toString(),
            resolver.resolve(credential, Optional.of(CoreAuthenticationTestUtils.getPrincipal()),
                Optional.of(new SimpleTestUsernamePasswordAuthenticationHandler()),
                Optional.of(CoreAuthenticationTestUtils.getService())).getId());
    }

    @Test
    public void verifySupport() {
        val credential = new X509CertificateCredential(new X509Certificate[]{VALID_CERTIFICATE});
        assertTrue(this.resolver.supports(credential));
    }

    @Test
    public void verifySupportFalse() {
        assertFalse(this.resolver.supports(new UsernamePasswordCredential()));
    }

    @Test
    public void verifyHexPrincipalOdd() {
        val r = new X509SerialNumberPrincipalResolver(resolutionContext);
        r.setRadix(16);
        r.setZeroPadding(true);
        val mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(300L));

        val principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("012c", principal);
    }

    @Test
    public void verifyHexPrincipalOddFalse() {
        val r = new X509SerialNumberPrincipalResolver(resolutionContext);
        r.setRadix(16);
        val mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(300L));

        val principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("12c", principal);
    }

    @Test
    public void verifyHexPrincipalEven() {
        val r = new X509SerialNumberPrincipalResolver(resolutionContext);
        r.setRadix(16);
        r.setZeroPadding(true);
        val mockCert = mock(X509Certificate.class);
        when(mockCert.getSerialNumber()).thenReturn(BigInteger.valueOf(60300L));

        val principal = r.resolvePrincipalInternal(mockCert);
        assertEquals("eb8c", principal);
    }
}
