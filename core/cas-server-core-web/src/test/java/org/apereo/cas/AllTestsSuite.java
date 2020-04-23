package org.apereo.cas;

import org.apereo.cas.view.CasReloadableMessageBundleTests;
import org.apereo.cas.web.RegisteredServiceResponseHeadersEnforcementFilterTests;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBeanTests;
import org.apereo.cas.web.SimpleUrlValidatorTests;
import org.apereo.cas.web.WebjarValidationTests;
import org.apereo.cas.web.support.WebUtilsTests;
import org.apereo.cas.web.view.DynamicHtmlViewTests;

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
    CasReloadableMessageBundleTests.class,
    RegisteredServiceResponseHeadersEnforcementFilterTests.class,
    SimpleUrlValidatorFactoryBeanTests.class,
    DynamicHtmlViewTests.class,
    WebUtilsTests.class,
    SimpleUrlValidatorTests.class,
    WebjarValidationTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
