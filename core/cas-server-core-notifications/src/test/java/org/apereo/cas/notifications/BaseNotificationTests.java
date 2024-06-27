package org.apereo.cas.notifications;

import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link BaseNotificationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class BaseNotificationTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        MailSenderValidatorAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    public static class SharedTestConfiguration {
    }
}
