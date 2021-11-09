package org.apereo.cas;

import org.apereo.cas.web.flow.BasicAuthenticationActionTests;
import org.apereo.cas.web.flow.BasicAuthenticationCasMultifactorWebflowCustomizerTests;
import org.apereo.cas.web.flow.BasicAuthenticationWebflowConfigurerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    BasicAuthenticationWebflowConfigurerTests.class,
    BasicAuthenticationCasMultifactorWebflowCustomizerTests.class,
    BasicAuthenticationActionTests.class
})
@Suite
public class AllTestsSuite {
}
