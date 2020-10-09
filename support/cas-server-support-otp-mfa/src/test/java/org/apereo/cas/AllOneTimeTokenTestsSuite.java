package org.apereo.cas;

import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepositoryTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowEventResolverTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllOneTimeTokenTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    CachingOneTimeTokenRepositoryTests.class,
    OneTimeTokenAuthenticationWebflowEventResolverTests.class,
    OneTimeTokenAuthenticationWebflowActionTests.class,
    OneTimeTokenAccountSaveRegistrationActionTests.class,
    OneTimeTokenAccountCheckRegistrationActionTests.class
})
@RunWith(JUnitPlatform.class)
public class AllOneTimeTokenTestsSuite {
}
