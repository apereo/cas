package org.apereo.cas;

import org.apereo.cas.notifications.CommunicationsManagerTests;
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
    GroovySmsSenderTests.class,
    RestfulSmsSenderTests.class
})
@RunWith(JUnitPlatform.class)
public class AllNotificationsTestsSuite {
}
