package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.revocation.checker.AbstractCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for {@link RevocationChecker} unit tests.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
public abstract class AbstractCRLRevocationCheckerTests {

    /**
     * Certificate to be tested.
     */
    private final X509Certificate[] certificates;

    /**
     * Expected result of check; null for success.
     */
    private final GeneralSecurityException expected;

    /**
     * Creates a new test instance with given parameters.
     *
     * @param certFiles File names of certificates to check.
     * @param expected  Expected result of check; null to indicate expected success.
     */
    public AbstractCRLRevocationCheckerTests(final String[] certFiles, final GeneralSecurityException expected) {
        this.expected = expected;
        this.certificates = new X509Certificate[certFiles.length];
        val i = new AtomicInteger();
        for (val file : certFiles) {
            this.certificates[i.getAndIncrement()] = CertUtils.readCertificate(new ClassPathResource(file));
        }
    }

    /**
     * Test method for {@link AbstractCRLRevocationChecker#check(X509Certificate)}.
     */
    @Test
    public void checkCertificate() {
        try {
            for (val cert : this.certificates) {
                getChecker().check(cert);
            }
            if (this.expected != null) {
                fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (final GeneralSecurityException e) {
            if (this.expected == null) {
                fail("Revocation check failed unexpectedly with exception: " + e);
            } else {
                val expectedClass = this.expected.getClass();
                val actualClass = e.getClass();
                assertTrue(
                    expectedClass.isAssignableFrom(actualClass),
                    String.format("Expected exception of type %s but got %s", expectedClass, actualClass));
            }
        }
    }

    protected abstract RevocationChecker getChecker();
}
