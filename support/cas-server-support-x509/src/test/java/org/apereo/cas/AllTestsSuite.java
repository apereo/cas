package org.apereo.cas;

import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcherTests;
import org.apereo.cas.adaptors.x509.config.DefaultX509ConfigTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses({
    LdaptiveResourceCRLFetcherTests.class,
    DefaultX509ConfigTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
