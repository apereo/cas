package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
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
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
import java.security.cert.X509Certificate;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * This is {@link X509CommonNameEDIPIPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("X509")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class X509CommonNameEDIPIPrincipalResolverTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Mock
    private ServicesManager servicesManager;

    @Mock
    private AttributeDefinitionStore attributeDefinitionStore;

    @Mock
    private AttributeRepositoryResolver attributeRepositoryResolver;

    @BeforeEach
    void before() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    private static X509Certificate getCertificateFrom(final String certPath) throws Exception {
        val certLocation = X509CommonNameEDIPIPrincipalResolverTests.class.getResource(certPath).getPath();
        return FunctionUtils.doUnchecked(() -> {
            try (val in = new FileInputStream(certLocation)) {
                return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
            }
        });
    }

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() throws Exception {
        return Stream.of(
            /*
             * test with cert with EDIPI and no alternate
             */
            arguments(
                getCertificateFrom("/edipi.cer"),
                "1234567890",
                null,
                new EDIPIX509AttributeExtractor(),
                true
            ),

            /*
             * test with alternate parameter and cert without EDIPI
             */
            arguments(
                getCertificateFrom("/user-valid.crt"),
                "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
                "subjectDn",
                new EDIPIX509AttributeExtractor(),
                false
            ),

            /*
             * test with cert with EDIPI and no alternate, default attribute extractor
             */
            arguments(
                getCertificateFrom("/edipi.cer"),
                "1234567890",
                null,
                new DefaultX509AttributeExtractor(),
                false
            ),

            /*
             * Test with no subject dn name and alternate.
             */
            arguments(
                new CasX509Certificate(true).setSubjectDn(StringUtils.EMPTY),
                "CN=Jasig,DC=jasig,DC=org",
                "issuerDn",
                new EDIPIX509AttributeExtractor(),
                false
            ),

            /*
             * Test with no common name, and alternate.
             */
            arguments(
                new CasX509Certificate(true).setSubjectDn("sample-subject-dn"),
                "CN=Jasig,DC=jasig,DC=org",
                "issuerDn",
                new EDIPIX509AttributeExtractor(),
                false
            )
        );

    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    void verifyResolvePrincipalInternal(final X509Certificate certificate,
                                        final String expectedResult,
                                        final String alternatePrincipalAttribute,
                                        final X509AttributeExtractor x509AttributeExtractor,
                                        final boolean edipiExpected) {

        val context = PrincipalResolutionContext.builder()
            .attributeDefinitionStore(attributeDefinitionStore)
            .servicesManager(servicesManager)
            .applicationContext(applicationContext)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .attributeMerger(CoreAuthenticationUtils.getAttributeMerger(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE))
            .attributeRepository(CoreAuthenticationTestUtils.getAttributeRepository())
            .principalFactory(PrincipalFactoryUtils.newPrincipalFactory())
            .returnNullIfNoAttributes(false)
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(false)
            .resolveAttributes(true)
            .activeAttributeRepositoryIdentifiers(CollectionUtils.wrapSet(PersonAttributeDao.WILDCARD))
            .build();

        val resolver = new X509CommonNameEDIPIPrincipalResolver(context);
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
        resolver.setX509AttributeExtractor(x509AttributeExtractor);

        val userId = resolver.resolvePrincipalInternal(certificate);
        assertEquals(expectedResult, userId);

        val attributes = resolver.extractPersonAttributes(certificate);
        if (edipiExpected) {
            assertEquals(attributes.get("x509EDIPI"), CollectionUtils.wrapList(expectedResult));
        } else {
            assertNull(attributes.get("x509EDIPI"));
        }
    }
}
