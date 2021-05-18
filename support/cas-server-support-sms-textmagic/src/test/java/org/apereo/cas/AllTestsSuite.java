package org.apereo.cas;

import org.apereo.cas.support.sms.TextMagicSmsConfigurationTests;
import org.apereo.cas.support.sms.TextMagicSmsSenderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    TextMagicSmsSenderTests.class,
    TextMagicSmsConfigurationTests.class
})
@Suite
public class AllTestsSuite {
}
