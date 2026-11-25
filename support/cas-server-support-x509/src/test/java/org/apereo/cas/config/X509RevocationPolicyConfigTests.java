package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.adaptors.x509.BaseX509Tests;
import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CertUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509RevocationPolicyConfigTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("X509")
@ExtendWith(CasTestExtension.class)
class X509RevocationPolicyConfigTests {

    @SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.x509.revocation-checker=resource",
            "cas.authn.x509.crl-resources=classpath:userCA-expired.crl"
        })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier("resourceCrlRevocationChecker")
        protected RevocationChecker resourceCrlRevocationChecker;

        @Autowired
        @Qualifier("crlDistributionPointRevocationChecker")
        protected RevocationChecker crlDistributionPointRevocationChecker;

        protected static X509Certificate getCertificate() {
            return getCertificate("user-valid.crt");
        }

        protected static X509Certificate getCertificate(final String name) {
            return CertUtils.readCertificate(new ClassPathResource(name));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.x509.crl-resource-expired-policy=allow")
    class AllowExpiredCrl extends BaseTests {
        @Test
        void verifyOperation() {
            val certificate = getCertificate();
            assertDoesNotThrow(() -> resourceCrlRevocationChecker.check(certificate));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.x509.crl-resource-expired-policy=deny")
    class DenyExpiredCrl extends BaseTests {
        @Test
        void verifyOperation() {
            val certificate = getCertificate();
            val error = assertThrows(GeneralSecurityException.class, () -> resourceCrlRevocationChecker.check(certificate));
            assertEquals("Aborting since DenyRevocationPolicy is in effect.", error.getMessage());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.authn.x509.crl-resource-unavailable-policy=threshold",
        "cas.authn.x509.crl-unavailable-policy=threshold"
    })
    class ThresholdUnavailableCrl extends BaseTests {
        @Test
        void verifyResourceChecker() {
            val certificate = getCertificate("userCA.crt");
            val error = assertThrows(GeneralSecurityException.class, () -> resourceCrlRevocationChecker.check(certificate));
            assertEquals("Aborting since DenyRevocationPolicy is in effect.", error.getMessage());
        }

        @Test
        void verifyDistributionPointChecker() {
            val certificate = getCertificate("userCA.crt");
            val error = assertThrows(GeneralSecurityException.class, () -> crlDistributionPointRevocationChecker.check(certificate));
            assertEquals("Aborting since DenyRevocationPolicy is in effect.", error.getMessage());
        }
    }
}
