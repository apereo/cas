package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
public class X509CommonNameEDIPIPrincipalResolverTests {
    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        return Stream.of(
            /*
             * test with cert with EDIPI and no alternate
             */
            arguments(
                "/edipi.cer",
                "1234567890",
                null,
                new EDIPIX509AttributeExtractor(),
                true
            ),

            /*
             * test with alternate parameter and cert without EDIPI
             */
            arguments(
                "/user-valid.crt",
                "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
                "subjectDn",
                new EDIPIX509AttributeExtractor(),
                false
            ),
            /*
             * test with cert with EDIPI and no alternate, default attribute extractor
             */
            arguments(
                    "/edipi.cer",
                    "1234567890",
                    null,
                    new DefaultX509AttributeExtractor(),
                    false
            )
        );

    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String expectedResult,
                                               final String alternatePrincipalAttribute,
                                               final X509AttributeExtractor x509AttributeExtractor,
                                               final boolean edipiExpected) throws Exception {

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

        val resolver = new X509CommonNameEDIPIPrincipalResolver(context);
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
        resolver.setX509AttributeExtractor(x509AttributeExtractor);
        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
            new FileInputStream(getClass().getResource(certPath).getPath()));

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
