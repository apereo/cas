package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.ServletEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
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
    PropertyPlaceholderAutoConfiguration.class,
    AopAutoConfiguration.class,
    CompositeMeterRegistryAutoConfiguration.class,
    DispatcherServletAutoConfiguration.class,
    IntegrationAutoConfiguration.class,
    JacksonAutoConfiguration.class,
    MetricsAutoConfiguration.class,
    MockMvcAutoConfiguration.class,
    MustacheAutoConfiguration.class,
    ObservationAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    SecurityAutoConfiguration.class,
    SimpleMetricsExportAutoConfiguration.class,
    ThymeleafAutoConfiguration.class,
    ErrorMvcAutoConfiguration.class,
    TransactionAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class,
    WebClientAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    WebMvcEndpointManagementContextConfiguration.class,
    ServletEndpointManagementContextConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    MetricsEndpointAutoConfiguration.class,
    BeansEndpointAutoConfiguration.class,
    ConditionsReportEndpointAutoConfiguration.class,
    EnvironmentEndpointAutoConfiguration.class,
    HealthEndpointAutoConfiguration.class,
    InfoEndpointAutoConfiguration.class,
    SslAutoConfiguration.class
})
@Inherited
@EnableScheduling
@EnableTransactionManagement
@EnableRetry
@EnableConfigurationProperties({
    ManagementServerProperties.class,
    WebEndpointProperties.class,
    WebProperties.class,
    ServerProperties.class,
    MailProperties.class,
    SecurityProperties.class,
    CasConfigurationProperties.class
})
public @interface SpringBootTestAutoConfigurations {
}
