package org.apereo.cas.adaptors.duo.web;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link DuoSecurityAllTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DuoSecurityPingEndpointTests.class,
    DuoSecurityUserAccountStatusEndpointTests.class
})
public class DuoSecurityAllTests {
}
