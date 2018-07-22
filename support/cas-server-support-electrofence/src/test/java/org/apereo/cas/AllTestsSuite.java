package org.apereo.cas;

import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.notify.AuthenticationRiskEmailNotifierTests;
import org.apereo.cas.impl.notify.AuthenticationRiskSmsNotifierTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all test in a batch.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DateTimeAuthenticationRequestRiskCalculatorTests.class,
    GeoLocationAuthenticationRequestRiskCalculatorTests.class,
    IpAddressAuthenticationRequestRiskCalculatorTests.class,
    UserAgentAuthenticationRequestRiskCalculatorTests.class,
    AuthenticationRiskEmailNotifierTests.class,
    AuthenticationRiskSmsNotifierTests.class
})
public class AllTestsSuite {
}
