package org.apereo.cas.adaptors.duo.web;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class DuoSecurityAllTestsSuite {
}
