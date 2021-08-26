package org.apereo.cas;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProviderFactoryTests;
import org.apereo.cas.adaptors.duo.authn.UniversalPromptDuoSecurityAuthenticationServiceTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityAuthenticationWebflowEventResolverTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.duo.web.flow.DuoSecurityUniversalPromptMultifactorWebflowConfigurerTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityAuthenticationWebflowActionTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountActionTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDirectAuthenticationActionTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityPrepareWebLoginFormActionTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptPrepareLoginActionTests;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityUniversalPromptValidateLoginActionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link org.apereo.cas.AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    DuoSecurityAuthenticationWebflowEventResolverTests.class,
    DuoSecurityMultifactorAuthenticationProviderFactoryTests.class,
    DuoSecurityUniversalPromptMultifactorWebflowConfigurerTests.class,
    DuoSecurityMultifactorWebflowConfigurerTests.class,
    DuoSecurityUniversalPromptPrepareLoginActionTests.class,
    DuoSecurityUniversalPromptValidateLoginActionTests.class,
    UniversalPromptDuoSecurityAuthenticationServiceTests.class,
    DuoSecurityPrepareWebLoginFormActionTests.class,
    DuoSecurityDirectAuthenticationActionTests.class,
    DuoSecurityAuthenticationWebflowActionTests.class,
    DuoSecurityDetermineUserAccountActionTests.class
})
@Suite
public class AllTestsSuite {
}
