package org.apereo.cas.adaptors.duo.web;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link DuoSecurityAllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DuoSecurityPingEndpointTests.class,
    DuoSecurityUserAccountStatusEndpointTests.class
})
public class DuoSecurityAllTestsSuite {
}
