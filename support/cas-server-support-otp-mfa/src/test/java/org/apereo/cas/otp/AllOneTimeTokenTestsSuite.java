package org.apereo.cas.otp;

import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepositoryTests;
import org.apereo.cas.otp.web.flow.rest.OneTimeTokenQRGeneratorControllerTests;

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
    OneTimeTokenQRGeneratorControllerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllOneTimeTokenTestsSuite {
}
