package org.apereo.cas;

import org.apereo.cas.web.flow.SurrogateWebflowConfigurerTests;
import org.apereo.cas.web.flow.action.LoadSurrogatesListActionTests;
import org.apereo.cas.web.flow.action.SurrogateAuthorizationActionTests;
import org.apereo.cas.web.flow.action.SurrogateInitialAuthenticationActionTests;
import org.apereo.cas.web.flow.action.SurrogateSelectionActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link SurrogateTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    SurrogateInitialAuthenticationActionTests.class,
    SurrogateSelectionActionTests.class,
    SurrogateWebflowConfigurerTests.class,
    SurrogateAuthorizationActionTests.class,
    LoadSurrogatesListActionTests.class
})
@RunWith(JUnitPlatform.class)
public class SurrogateTestsSuite {
}
