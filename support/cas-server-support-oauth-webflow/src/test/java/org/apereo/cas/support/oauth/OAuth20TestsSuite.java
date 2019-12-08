package org.apereo.cas.support.oauth;

import org.apereo.cas.support.oauth.web.flow.OAuth20RegisteredServiceUIActionTests;
import org.apereo.cas.support.oauth.web.flow.OAuth20WebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link OAuth20TestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    OAuth20RegisteredServiceUIActionTests.class,
    OAuth20WebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class OAuth20TestsSuite {
}
