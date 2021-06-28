package org.apereo.cas;

import org.apereo.cas.discovery.CasServerDiscoveryProfileEndpointTests;
import org.apereo.cas.discovery.CasServerProfileRegistrarTests;

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
