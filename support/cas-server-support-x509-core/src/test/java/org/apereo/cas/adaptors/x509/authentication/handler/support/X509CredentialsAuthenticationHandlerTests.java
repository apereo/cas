package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.adaptors.x509.authentication.principal.X509CertificateCredential;
import org.apereo.cas.adaptors.x509.authentication.revocation.RevokedCertificateException;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.ResourceCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.policy.ThresholdExpiredCRLRevocationPolicy;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.val;
import org.cryptacular.util.CertUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ClassPathResource;

import javax.security.auth.login.FailedLoginException;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.apereo.cas.util.junit.Assertions.assertThrowsOrNot;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Unit test for {@link X509CredentialsAuthenticationHandler} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Tag("X509")
public class X509CredentialsAuthenticationHandlerTests {

    private static final String USER_VALID_CRT = "user-valid.crt";

    /**
     * Gets the unit test parameters.
     *
     * @return Test parameter data.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static Stream<Arguments> getTestParameters() {
        val params = new ArrayList<Arguments>();

        /* Test case #1: Unsupported credential type */
        var handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        params.add(arguments(
            handler,
            new UsernamePasswordCredential(),
            false,
            null,
            null));

        /* Test case #2:Valid certificate /*/
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        var credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(arguments(
            handler,
            credential,
            true,
            new DefaultAuthenticationHandlerExecutionResult(handler, credential, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(credential.getId())),
            null));

        /* Test case #3: Expired certificate */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"));
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates("user-expired.crt")),
            true,
            null,
            new CertificateExpiredException()));

        /* Test case #4: Untrusted issuer */
        handler = new X509CredentialsAuthenticationHandler(
            RegexUtils.createPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US"),
            true, false, false);
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates("snake-oil.crt")),
            true,
            null,
            new FailedLoginException()));

        /* Test case #5: Disallowed subject  */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
            true,
            RegexUtils.createPattern("CN=\\w+,OU=CAS,O=Jasig,L=Westminster,ST=Colorado,C=US"));
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates("snake-oil.crt")),
            true,
            null,
            new FailedLoginException()));

        /* Test case #6: Check key usage on a cert without keyUsage extension */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
            false, true, false);
        credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(arguments(
            handler,
            credential,
            true,
            new DefaultAuthenticationHandlerExecutionResult(handler, credential, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(credential.getId())),
            null));

        /* Test case #7: Require key usage on a cert without keyUsage extension */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
            false, true, true);
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
            true,
            null,
            new FailedLoginException()));

        /* Test case #8: Require key usage on a cert with acceptable keyUsage extension values */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
            false, true, true);
        credential = new X509CertificateCredential(createCertificates("user-valid-keyUsage.crt"));
        params.add(arguments(
            handler,
            credential,
            true,
            new DefaultAuthenticationHandlerExecutionResult(handler, credential, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(credential.getId())),
            null
        ));

        /* Test case #9: Require key usage on a cert with unacceptable keyUsage extension values */
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"),
            false, true, true);
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates("user-invalid-keyUsage.crt")),
            true,
            null,
            new FailedLoginException()));

        /*
         * Revocation tests
         */

        /* Test case #10: Valid certificate with CRL checking */
        var checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        credential = new X509CertificateCredential(createCertificates(USER_VALID_CRT));
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
            true,
            new DefaultAuthenticationHandlerExecutionResult(handler, credential, PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(credential.getId())),
            null
        ));

        /* Test case #11: Revoked end user certificate */
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-valid.crl"));
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates("user-revoked.crt")),
            true,
            null,
            new RevokedCertificateException(ZonedDateTime.now(ZoneOffset.UTC), null)
        ));

        /* Test case #12: Valid certificate on expired CRL data */
        val zeroThresholdPolicy = new ThresholdExpiredCRLRevocationPolicy(0);
        checker = new ResourceCRLRevocationChecker(new ClassPathResource("userCA-expired.crl"), null, zeroThresholdPolicy);
        checker.init();
        handler = new X509CredentialsAuthenticationHandler(RegexUtils.createPattern(".*"), checker);
        params.add(arguments(
            handler,
            new X509CertificateCredential(createCertificates(USER_VALID_CRT)),
            true,
            null,
            new ExpiredCRLException(null, ZonedDateTime.now(ZoneOffset.UTC))
        ));

        return params.stream();
    }

    protected static X509Certificate[] createCertificates(final String... files) {
        val certs = new X509Certificate[files.length];

        var i = 0;
        for (val file : files) {
            try {
                certs[i++] = CertUtil.readCertificate(new ClassPathResource(file).getInputStream());
            } catch (final Exception e) {
                throw new IllegalArgumentException("Error creating certificate at " + file, e);
            }
        }
        return certs;
    }

    /**
     * Tests the {@link X509CredentialsAuthenticationHandler#authenticate(Credential)} method.
     */
    @ParameterizedTest
    @MethodSource("getTestParameters")
    public void verifyAuthenticate(final X509CredentialsAuthenticationHandler handler, final Credential credential,
                                   final boolean expectedSupports, final AuthenticationHandlerExecutionResult expectedResult,
                                   final GeneralSecurityException expectedException) {
        assertThrowsOrNot(expectedException, () -> {
            if (expectedSupports) {
                assertTrue(handler.supports(credential));
                val result = handler.authenticate(credential);
                assertEquals(expectedResult, result);
            }
        });

        assertEquals(expectedSupports, handler.supports(credential));
    }
}

