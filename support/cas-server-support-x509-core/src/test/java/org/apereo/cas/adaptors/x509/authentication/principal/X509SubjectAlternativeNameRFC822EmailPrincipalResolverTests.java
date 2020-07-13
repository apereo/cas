package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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
                                               final String requiredAttribute) throws FileNotFoundException, CertificateException {
        val resolver = new X509SubjectAlternativeNameRFC822EmailPrincipalResolver();
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);
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
                assertTrue(principal.getAttributes().keySet().contains(requiredAttribute));
            }
        } else {
            assertNull(principal);
        }
    }
}
