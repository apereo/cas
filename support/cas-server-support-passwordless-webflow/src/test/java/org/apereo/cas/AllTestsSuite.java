package org.apereo.cas;

import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationActionTests;

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
    PrepareForPasswordlessAuthenticationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
