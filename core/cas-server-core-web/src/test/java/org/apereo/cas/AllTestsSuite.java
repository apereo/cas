
package org.apereo.cas;

import org.apereo.cas.web.CasCoreViewsConfigurationTests;
import org.apereo.cas.web.RegisteredServiceResponseHeadersEnforcementFilterTests;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBeanTests;
import org.apereo.cas.web.WebjarValidationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    RegisteredServiceResponseHeadersEnforcementFilterTests.class,
    SimpleUrlValidatorFactoryBeanTests.class,
    WebjarValidationTests.class,
    CasCoreViewsConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
