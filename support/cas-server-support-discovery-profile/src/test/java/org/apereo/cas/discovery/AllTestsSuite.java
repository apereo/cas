package org.apereo.cas.discovery;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link org.apereo.cas.AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CasServerDiscoveryProfileEndpointTests.class,
    CasServerProfileRegistrarTests.class
})
@Suite
public class AllTestsSuite {
}
