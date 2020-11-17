package org.apereo.cas;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProviderFactoryTests;
import org.apereo.cas.adaptors.duo.authn.UniversalPromptDuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityDetermineUserAccountActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityDirectAuthenticationActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityPrepareWebLoginFormActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityUniversalPromptPrepareLoginActionTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityUniversalPromptValidateLoginActionTests;

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
    DuoSecurityMultifactorAuthenticationProviderFactoryTests.class,
    DuoSecurityMultifactorWebflowConfigurerTests.class,
    DuoSecurityUniversalPromptPrepareLoginActionTests.class,
    DuoSecurityUniversalPromptValidateLoginActionTests.class,
    UniversalPromptDuoSecurityAuthenticationServiceTests.class,
    DuoSecurityPrepareWebLoginFormActionTests.class,
    DuoSecurityDirectAuthenticationActionTests.class,
    DuoSecurityAuthenticationWebflowActionTests.class,
    DuoSecurityDetermineUserAccountActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
