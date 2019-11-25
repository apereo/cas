package org.apereo.cas;

import org.apereo.cas.web.flow.BasicAuthenticationActionTests;
import org.apereo.cas.web.flow.BasicAuthenticationWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    BasicAuthenticationWebflowConfigurerTests.class,
    BasicAuthenticationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
