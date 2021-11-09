package org.apereo.cas.adaptors.x509;

import org.apereo.cas.adaptors.x509.authentication.revocation.checker.RevocationChecker;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509CrlDistributionCheckerCachingTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("X509")
@SpringBootTest(
    classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.x509.cache-disk-overflow=true",
        "cas.authn.x509.cache-eternal=true",
        "cas.authn.x509.revocation-checker=crl",
        "cas.authn.x509.crl-unavailable-policy=allow",
        "cas.authn.x509.crl-resource-expired-policy=threshold"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class X509CrlDistributionCheckerCachingTests {
    @Autowired
    @Qualifier("crlDistributionPointRevocationChecker")
    private RevocationChecker crlDistributionPointRevocationChecker;

    @Test
    public void verifyOperation() {
        assertNotNull(crlDistributionPointRevocationChecker);
    }
}
