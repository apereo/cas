package org.apereo.cas;

import org.apereo.cas.adaptors.x509.authentication.ldap.LdaptiveResourceCRLFetcherTests;
import org.apereo.cas.adaptors.x509.config.DefaultX509ConfigTests;
import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses({
    LdaptiveResourceCRLFetcherTests.class,
    DefaultX509ConfigTests.class,
})
public class AllTestsSuite {
}
