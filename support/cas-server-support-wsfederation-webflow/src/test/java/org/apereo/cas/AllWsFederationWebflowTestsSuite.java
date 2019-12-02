package org.apereo.cas;

import org.apereo.cas.web.flow.WsFederationActionTests;
import org.apereo.cas.web.flow.WsFederationWebflowConfigurerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllWsFederationWebflowTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    WsFederationActionTests.class,
    WsFederationWebflowConfigurerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllWsFederationWebflowTestsSuite {
}
