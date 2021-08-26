package org.apereo.cas;

import org.apereo.cas.config.TwilioSmsConfigurationTests;
import org.apereo.cas.support.sms.TwilioSmsSenderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    TwilioSmsSenderTests.class,
    TwilioSmsConfigurationTests.class
})
@Suite
public class AllTestsSuite {
}
