package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.authn.BasicDuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.authn.DefaultDuoSecurityAdminApiServiceTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationHandlerTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityAdminApiEndpointTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityPingEndpointTests;
import org.apereo.cas.adaptors.duo.web.DuoSecurityUserAccountStatusEndpointTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link DuoSecurityAllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DuoSecurityPingEndpointTests.class,
    DefaultDuoSecurityAdminApiServiceTests.class,
    DuoSecurityAdminApiEndpointTests.class,
    DuoSecurityAuthenticationServiceTests.class,
    BasicDuoSecurityAuthenticationServiceTests.class,
    DuoSecurityAuthenticationHandlerTests.class,
    DuoSecurityUserAccountStatusEndpointTests.class
})
@Suite
public class DuoSecurityAllTestsSuite {
}
