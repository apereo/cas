package org.apereo.cas.adaptors.x509.authentication.handler.support;

import org.apereo.cas.adaptors.x509.authentication.revocation.checker.AbstractCRLRevocationChecker;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.util.crypto.CertUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

/**
 * Base class for {@link RevocationChecker} unit tests.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public abstract class AbstractCRLRevocationCheckerTests {

    /** Certificate to be tested. */
    private final X509Certificate[] certificates;

    /** Expected result of check; null for success. */
    private final GeneralSecurityException expected;
    
    /**
     * Creates a new test instance with given parameters.
     *
     * @param certFiles File names of certificates to check.
     * @param expected Expected result of check; null to indicate expected success.
     */
    public AbstractCRLRevocationCheckerTests(final String[] certFiles, final GeneralSecurityException expected) {
        this.expected = expected;
        this.certificates = new X509Certificate[certFiles.length];
        int i = 0;
        for (final String file : certFiles) {
            this.certificates[i++] = CertUtils.readCertificate(new ClassPathResource(file));
        }
    }

    /**
     * Test method for {@link AbstractCRLRevocationChecker#check(X509Certificate)}.
     */
    @Test
    public void checkCertificate() {
        try {
            for (final X509Certificate cert : this.certificates) {
                getChecker().check(cert);
            }
            if (this.expected != null) {
                Assert.fail("Expected exception of type " + this.expected.getClass());
            }
        } catch (final GeneralSecurityException e) {
            if (this.expected == null) {
                Assert.fail("Revocation check failed unexpectedly with exception: " + e);
            } else {
                final Class<?> expectedClass = this.expected.getClass();
                final Class<?> actualClass = e.getClass();
                Assert.assertTrue(
                        String.format("Expected exception of type %s but got %s", expectedClass, actualClass),
                        expectedClass.isAssignableFrom(actualClass));
            }
        }
    }

    protected abstract RevocationChecker getChecker();
}
