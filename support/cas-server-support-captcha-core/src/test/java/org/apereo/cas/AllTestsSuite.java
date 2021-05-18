package org.apereo.cas;

import org.apereo.cas.web.CaptchaValidatorTests;
import org.apereo.cas.web.GoogleCaptchaV2ValidatorTests;
import org.apereo.cas.web.GoogleCaptchaV3ValidatorTests;
import org.apereo.cas.web.HCaptchaValidatorTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CaptchaValidatorTests.class,
    HCaptchaValidatorTests.class,
    GoogleCaptchaV2ValidatorTests.class,
    GoogleCaptchaV3ValidatorTests.class
})
@Suite
public class AllTestsSuite {
}
