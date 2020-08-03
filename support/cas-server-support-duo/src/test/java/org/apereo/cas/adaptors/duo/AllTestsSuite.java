package org.apereo.cas.adaptors.duo;

import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityDetermineUserAccountActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityPrepareWebLoginFormActionTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link org.apereo.cas.AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DuoSecurityAuthenticationWebflowEventResolverTests.class,
    DuoSecurityMultifactorWebflowConfigurerTests.class,
    DuoSecurityPrepareWebLoginFormActionTests.class,
    DuoSecurityDetermineUserAccountActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
