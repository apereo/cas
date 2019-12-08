
package org.apereo.cas;

import org.apereo.cas.adaptors.x509.web.extractcert.X509CertificateExtractorTests;
import org.apereo.cas.adaptors.x509.web.flow.X509CertificateCredentialsNonInteractiveActionTests;
import org.apereo.cas.adaptors.x509.web.flow.X509CertificateCredentialsRequestHeaderActionTests;
import org.apereo.cas.adaptors.x509.web.flow.X509WebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    X509CertificateExtractorTests.class,
    X509WebflowConfigurerTests.class,
    X509CertificateCredentialsNonInteractiveActionTests.class,
    X509CertificateCredentialsRequestHeaderActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
