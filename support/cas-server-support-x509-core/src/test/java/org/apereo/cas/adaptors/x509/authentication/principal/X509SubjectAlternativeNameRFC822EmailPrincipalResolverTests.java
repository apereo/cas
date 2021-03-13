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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link X509SubjectAlternativeNameUPNPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Tag("X509")
public class X509SubjectAlternativeNameRFC822EmailPrincipalResolverTests {

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    public static Stream<Arguments> getTestParameters() {
        return Stream.of(
            /* test with cert with RFC822 Email Address and no alternate */
            arguments(
                "/x509-san-upn-resolver.crt",
                "test@somecompany.com",
                null,
                "x509Rfc822Email"
            ),

            /* test with alternate parameter and cert with RFC822 Email Address */
            arguments(
                "/x509-san-upn-resolver.crt",
                "test@somecompany.com",
                "subjectDn",
                "x509subjectUPN"
            ),

            /* test with alternate parameter and cert without RFC822 Email Address */
            arguments(
                "/user-valid.crt",
                "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
                "subjectDn",
                null
            ),

            /* test with bad alternate parameter and cert without RFC822 Email Address */
            arguments(
                "/user-valid.crt",
                null,
                "badAttribute",
                null
            )
        );
    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String expectedResult,
                                               final String alternatePrincipalAttribute,
                                               final String requiredAttribute) throws Exception {

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

        val resolver = new X509SubjectAlternativeNameRFC822EmailPrincipalResolver(context);
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
            new FileInputStream(getClass().getResource(certPath).getPath()));

        val userId = resolver.resolvePrincipalInternal(certificate);
        assertEquals(expectedResult, userId);

        val credential = new X509CertificateCredential(new X509Certificate[]{certificate});
        credential.setCertificate(certificate);
        val principal = resolver.resolve(credential);
        if (expectedResult != null) {
            assertNotNull(principal);
            assertFalse(principal.getAttributes().isEmpty());
            if (requiredAttribute != null) {
                assertTrue(principal.getAttributes().containsKey(requiredAttribute));
            }
        } else {
            assertNull(principal);
        }
    }

    @Test
    public void verifyAlternate() throws Exception {
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

        val resolver = new X509SubjectAlternativeNameRFC822EmailPrincipalResolver(context);
        resolver.setX509AttributeExtractor(new DefaultX509AttributeExtractor());
        val certificate = mock(X509Certificate.class);
        when(certificate.getSubjectAlternativeNames()).thenThrow(new CertificateParsingException());
        assertNull(resolver.resolvePrincipalInternal(certificate));
    }
}
