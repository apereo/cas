package org.apereo.cas;

import org.apereo.cas.adaptors.x509.RequestHeaderX509CertificateExtractorTests;
import org.apereo.cas.adaptors.x509.X509CrlDistributionCheckerCachingTests;
import org.apereo.cas.adaptors.x509.X509SubjectDNPrincipalResolverAggregateTests;
import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcherTests;
import org.apereo.cas.adaptors.x509.config.DefaultX509ConfigTests;
import org.apereo.cas.adaptors.x509.config.EDIPIX509AttributeExtractorConfigTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses({
    LdaptiveResourceCRLFetcherTests.class,
    DefaultX509ConfigTests.class,
    X509CrlDistributionCheckerCachingTests.class,
    EDIPIX509AttributeExtractorConfigTests.class,
    RequestHeaderX509CertificateExtractorTests.class,
    X509SubjectDNPrincipalResolverAggregateTests.class
})
@Suite
public class AllTestsSuite {
}
