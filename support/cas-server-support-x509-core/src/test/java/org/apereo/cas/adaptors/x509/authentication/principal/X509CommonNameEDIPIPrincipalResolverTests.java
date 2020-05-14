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
                null
            ),

            /*
             * test with alternate parameter and cert without EDIPI
             */
            arguments(
                "/user-valid.crt",
                "CN=Alice, OU=CAS, O=Jasig, L=Westminster, ST=Colorado, C=US",
                "subjectDn"
            )
        );

    }

    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyResolvePrincipalInternal(final String certPath,
                                               final String expectedResult,
                                               final String alternatePrincipalAttribute) throws FileNotFoundException, CertificateException {
        val resolver = new X509CommonNameEDIPIPrincipalResolver();
        resolver.setAlternatePrincipalAttribute(alternatePrincipalAttribute);

        val certificate = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(
            new FileInputStream(getClass().getResource(certPath).getPath()));

        val userId = resolver.resolvePrincipalInternal(certificate);
        assertEquals(expectedResult, userId);
    }
}
