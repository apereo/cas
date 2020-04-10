
package org.apereo.cas;

import org.apereo.cas.support.x509.rest.X509RestHttpRequestHeaderCredentialFactoryTests;
import org.apereo.cas.support.x509.rest.X509RestMultipartBodyCredentialFactoryTests;
import org.apereo.cas.support.x509.rest.X509RestTlsClientCertCredentialFactoryTests;
import org.apereo.cas.support.x509.rest.config.X509RestConfigurationTests;

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
    X509RestConfigurationTests.class,
    X509RestHttpRequestHeaderCredentialFactoryTests.class,
    X509RestMultipartBodyCredentialFactoryTests.class,
    X509RestTlsClientCertCredentialFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
