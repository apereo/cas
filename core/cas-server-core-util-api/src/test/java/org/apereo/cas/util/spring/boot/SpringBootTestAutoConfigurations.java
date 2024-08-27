package org.apereo.cas.util.spring.boot;

import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is {@link SpringBootTestAutoConfigurations}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ImportAutoConfiguration(classes = {
    AopAutoConfiguration.class,
    BeansEndpointAutoConfiguration.class,
    CompositeMeterRegistryAutoConfiguration.class,
    ConditionsReportEndpointAutoConfiguration.class,
    DispatcherServletAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    EnvironmentEndpointAutoConfiguration.class,
    ErrorMvcAutoConfiguration.class,
    HealthEndpointAutoConfiguration.class,
    InfoEndpointAutoConfiguration.class,
    IntegrationAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class,
    MetricsAutoConfiguration.class,
    MetricsEndpointAutoConfiguration.class,
    MockMvcAutoConfiguration.class,
    MustacheAutoConfiguration.class,
    ObservationAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    ServletEndpointManagementContextConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class,
    ThymeleafAutoConfiguration.class,
    TransactionAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class,
    WebClientAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    WebMvcEndpointManagementContextConfiguration.class
})
@Inherited
public @interface SpringBootTestAutoConfigurations {
}
