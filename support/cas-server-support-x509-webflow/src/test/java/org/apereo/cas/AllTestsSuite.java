
package org.apereo.cas;

import org.apereo.cas.adaptors.x509.web.X509TomcatServletWebServiceFactoryWebflowConfigurerTests;
import org.apereo.cas.adaptors.x509.web.extractcert.X509CertificateExtractorTests;
import org.apereo.cas.adaptors.x509.web.flow.X509CertificateCredentialsNonInteractiveActionTests;
import org.apereo.cas.adaptors.x509.web.flow.X509CertificateCredentialsRequestHeaderActionTests;
import org.apereo.cas.adaptors.x509.web.flow.X509TomcatServletFactoryInitialActionTests;
import org.apereo.cas.adaptors.x509.web.flow.X509WebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    X509CertificateExtractorTests.class,
    X509WebflowConfigurerTests.class,
    X509TomcatServletWebServiceFactoryWebflowConfigurerTests.class,
    X509TomcatServletFactoryInitialActionTests.class,
    X509CertificateCredentialsNonInteractiveActionTests.class,
    X509CertificateCredentialsRequestHeaderActionTests.class
})
@Suite
public class AllTestsSuite {
}
