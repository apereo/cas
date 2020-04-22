package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.BaseDuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.authn.BasicDuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandlerTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpointTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpointTests;

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
    BaseDuoSecurityAuthenticationServiceTests.class,
    BasicDuoSecurityAuthenticationServiceTests.class,
    DuoSecurityAuthenticationHandlerTests.class,
    DuoSecurityUserAccountStatusEndpointTests.class
})
@RunWith(JUnitPlatform.class)
public class DuoSecurityAllTestsSuite {
}
