package org.apereo.cas;

import org.apereo.cas.config.AmazonSimpleNotificationServiceSmsConfigurationTests;
import org.apereo.cas.support.sms.AmazonSimpleNotificationServiceSmsSenderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    AmazonSimpleNotificationServiceSmsSenderTests.class,
    AmazonSimpleNotificationServiceSmsConfigurationTests.class
})
@Suite
public class AllTestsSuite {
}
