package org.apereo.cas;

import org.apereo.cas.config.CasCoreNotificationsConfigurationTests;
import org.apereo.cas.notifications.CommunicationsManagerTests;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilderTests;
import org.apereo.cas.notifications.push.DefaultNotificationSenderTests;
import org.apereo.cas.notifications.sms.GroovySmsSenderTests;
import org.apereo.cas.notifications.sms.RestfulSmsSenderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    EmailMessageBodyBuilderTests.class,
    CasCoreNotificationsConfigurationTests.class,
    RestfulSmsSenderTests.class
})
@Suite
public class AllNotificationsTestsSuite {
}
