package org.apereo.cas;

import org.apereo.cas.web.flow.WsFederationActionTests;
import org.apereo.cas.web.flow.WsFederationResponseValidatorTests;
import org.apereo.cas.web.flow.WsFederationWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllWsFederationWebflowTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    WsFederationActionTests.class,
    WsFederationResponseValidatorTests.class,
    WsFederationWebflowConfigurerTests.class
})
@Suite
public class AllWsFederationWebflowTestsSuite {
}
