package org.apereo.cas;

import org.apereo.cas.config.CasCoreNotificationsConfigurationTests;
import org.apereo.cas.notifications.CommunicationsManagerTests;
import org.apereo.cas.notifications.push.DefaultNotificationSenderTests;
import org.apereo.cas.sms.GroovySmsSenderTests;
import org.apereo.cas.sms.RestfulSmsSenderTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllNotificationsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    CommunicationsManagerTests.class,
    DefaultNotificationSenderTests.class,
    GroovySmsSenderTests.class,
    CasCoreNotificationsConfigurationTests.class,
    RestfulSmsSenderTests.class
})
@RunWith(JUnitPlatform.class)
public class AllNotificationsTestsSuite {
}
