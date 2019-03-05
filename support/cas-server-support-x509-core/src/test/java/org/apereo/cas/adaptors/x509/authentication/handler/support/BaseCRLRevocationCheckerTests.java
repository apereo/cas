package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.revocation.checker.AbstractCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.springframework.core.io.ClassPathResource;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apereo.cas.util.junit.Assertions.*;

/**
 * Base class for {@link RevocationChecker} unit tests.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public abstract class BaseCRLRevocationCheckerTests {
    /**
     * Test method for {@link AbstractCRLRevocationChecker#check(X509Certificate)}.
     */
    protected static void checkCertificate(final AbstractCRLRevocationChecker checker, final String[] certFiles, final GeneralSecurityException expected) {
        val certificates = Arrays.stream(certFiles).map(file -> CertUtils.readCertificate(new ClassPathResource(file))).collect(Collectors.toList());

        assertThrowsOrNot(expected, () -> {
            for (val cert : certificates) {
                checker.check(cert);
            }
        });
    }
}
