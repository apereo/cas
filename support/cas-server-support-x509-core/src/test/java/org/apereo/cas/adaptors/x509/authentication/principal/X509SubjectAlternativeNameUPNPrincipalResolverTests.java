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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

/**
 * Unit test for {@link X509SubjectAlternativeNameUPNPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 3.0.0
 */
@Tag("X509")
public class X509SubjectAlternativeNameUPNPrincipalResolverTests {

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
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String expectedResult,
                                               final String alternatePrincipalAttribute) throws FileNotFoundException, CertificateException {

        val resolver = new X509SubjectAlternativeNameUPNPrincipalResolver();
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
        } else {
            assertNull(principal);
        }
    }
}
