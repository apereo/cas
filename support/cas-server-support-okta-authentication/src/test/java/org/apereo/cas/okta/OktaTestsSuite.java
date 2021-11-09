package org.apereo.cas.okta;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OktaTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    OktaPersonAttributeDaoTests.class,
    OktaConfigurationFactoryTests.class,
    OktaAuthenticationStateHandlerTests.class,
    OktaAuthenticationStateHandlerAdapterTests.class
})
@Suite
public class OktaTestsSuite {
}
