package org.apereo.cas;

import org.apereo.cas.web.flow.AcceptPasswordlessAccountAuthenticationActionTests;
import org.apereo.cas.web.flow.DetermineDelegatedAuthenticationActionTests;
import org.apereo.cas.web.flow.DetermineMultifactorPasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.VerifyPasswordlessAccountAuthenticationActionTests;

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
    PasswordlessAuthenticationWebflowConfigurerTests.class,
    DisplayBeforePasswordlessAuthenticationActionTests.class,
    VerifyPasswordlessAccountAuthenticationActionTests.class,
    DetermineDelegatedAuthenticationActionTests.class,
    DetermineMultifactorPasswordlessAuthenticationActionTests.class,
    AcceptPasswordlessAccountAuthenticationActionTests.class,
    PrepareForPasswordlessAuthenticationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
