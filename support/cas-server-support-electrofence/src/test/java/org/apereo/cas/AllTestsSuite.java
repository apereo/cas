package org.apereo.cas;

import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.notify.AuthenticationRiskEmailNotifierTests;
import org.apereo.cas.impl.notify.AuthenticationRiskSmsNotifierTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SelectClasses({
    DateTimeAuthenticationRequestRiskCalculatorTests.class,
    GeoLocationAuthenticationRequestRiskCalculatorTests.class,
    IpAddressAuthenticationRequestRiskCalculatorTests.class,
    UserAgentAuthenticationRequestRiskCalculatorTests.class,
    AuthenticationRiskEmailNotifierTests.class,
    AuthenticationRiskSmsNotifierTests.class
})
public class AllTestsSuite {
}
