package org.apereo.cas;

import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepositoryTests;
import org.apereo.cas.otp.util.QRUtilsTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCheckRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowActionTests;
import org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowEventResolverTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllOneTimeTokenTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    QRUtilsTests.class,
    CachingOneTimeTokenRepositoryTests.class,
    OneTimeTokenAccountConfirmSelectionRegistrationActionTests.class,
    OneTimeTokenAccountCreateRegistrationActionTests.class,
    OneTimeTokenAuthenticationWebflowEventResolverTests.class,
    OneTimeTokenAuthenticationWebflowActionTests.class,
    OneTimeTokenAccountSaveRegistrationActionTests.class,
    OneTimeTokenAccountCheckRegistrationActionTests.class
})
@Suite
public class AllOneTimeTokenTestsSuite {
}
