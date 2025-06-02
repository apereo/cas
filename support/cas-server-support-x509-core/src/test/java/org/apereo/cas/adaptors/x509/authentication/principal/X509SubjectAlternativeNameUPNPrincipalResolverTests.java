package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link X509SubjectAlternativeNameUPNPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Tag("X509")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class X509SubjectAlternativeNameUPNPrincipalResolverTests {

    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @BeforeEach
    void before() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        return Stream.of(
            /* test with cert with UPN and no alternate */
            arguments(
                "/x509-san-upn-resolver.crt",
                "test-user@some-company-domain",
                null
            ),

            /* test with alternate parameter and cert with UPN */
            arguments(
                "/x509-san-upn-resolver.crt",
                "test-user@some-company-domain",
                "subjectDn"
            ),

            /* test with alternate parameter and cert without UPN */
            arguments(
                "/user-valid.crt",
                "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
                "subjectDn"
            ),

            /* test with bad alternate parameter and cert without UPN */
            arguments(
                "/user-valid.crt",
                null,
                "badAttribute"
            )
        );

    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    void verifyResolvePrincipalInternal(final String certPath,
                                        final String expectedResult,
                                        final String alternatePrincipalAttribute) throws Throwable {

        val context = PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .servicesManager(servicesManager)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .applicationContext(applicationContext)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = new X509SubjectAlternativeNameUPNPrincipalResolver(context);
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
        val certLocation = getClass().getResource(certPath).getPath();
        val certificate = (X509Certificate) FunctionUtils.doUnchecked(() -> {
            try (val in = new FileInputStream(certLocation)) {
                return CertificateFactory.getInstance("X509").generateCertificate(in);
            }
        });

        val userId = resolver.resolvePrincipalInternal(certificate);
        assertEquals(expectedResult, userId);

        val credential = new X509CertificateCredential(new X509Certificate[]{certificate});
        credential.setCertificate(certificate);
        val principal = resolver.resolve(credential);
        if (expectedResult != null) {
            assertNotNull(principal);
            assertFalse(principal.getAttributes().isEmpty());
        } else {
            assertNull(principal);
        }
    }

    @Test
    void verifyAlternate() throws Throwable {
        val context = PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .servicesManager(servicesManager)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .applicationContext(applicationContext)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();
        val resolver = new X509SubjectAlternativeNameUPNPrincipalResolver(context);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
        val certificate = mock(X509Certificate.class);
        when(certificate.getSubjectAlternativeNames()).thenThrow(new CertificateParsingException());
        assertNull(resolver.resolvePrincipalInternal(certificate));
    }
}
