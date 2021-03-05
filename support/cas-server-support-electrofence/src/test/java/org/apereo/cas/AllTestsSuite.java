package org.apereo.cas;

import org.apereo.cas.impl.calcs.DateTimeAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.GeoLocationAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.IpAddressAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.calcs.UserAgentAuthenticationRequestRiskCalculatorTests;
import org.apereo.cas.impl.notify.AuthenticationRiskEmailNotifierTests;
import org.apereo.cas.impl.notify.AuthenticationRiskSmsNotifierTests;
import org.apereo.cas.impl.plans.MultifactorAuthenticationContingencyPlanTests;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.RiskAwareAuthenticationWebflowEventResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    RiskAwareAuthenticationWebflowConfigurerTests.class,
    AuthenticationRiskEmailNotifierTests.class,
    MultifactorAuthenticationContingencyPlanTests.class,
    RiskAwareAuthenticationWebflowEventResolverTests.class,
    AuthenticationRiskSmsNotifierTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
