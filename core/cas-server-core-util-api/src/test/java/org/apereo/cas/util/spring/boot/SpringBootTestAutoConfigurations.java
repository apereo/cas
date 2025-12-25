package org.apereo.cas.util.spring.boot;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.actuate.autoconfigure.beans.BeansEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.ssl.SslAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration;
import org.springframework.boot.integration.autoconfigure.IntegrationAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsEndpointAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.mustache.autoconfigure.MustacheAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.servlet.autoconfigure.actuate.web.ServletEndpointManagementContextConfiguration;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;
import org.springframework.boot.tomcat.autoconfigure.TomcatServerProperties;
import org.springframework.boot.tomcat.autoconfigure.servlet.TomcatServletWebServerAutoConfiguration;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.autoconfigure.servlet.ServletWebServerConfiguration;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.actuate.web.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcAutoConfiguration;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
    TomcatServletWebServerAutoConfiguration.class,
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
    WebMvcAutoConfiguration.class,
    EndpointAutoConfiguration.class,
    RefreshEndpointAutoConfiguration.class,
    WebEndpointAutoConfiguration.class,
    WebMvcEndpointManagementContextConfiguration.class,
    ServletEndpointManagementContextConfiguration.class,
    MetricsEndpointAutoConfiguration.class,
    BeansEndpointAutoConfiguration.class,
    ConditionsReportEndpointAutoConfiguration.class,
    EnvironmentEndpointAutoConfiguration.class,
    HealthContributorRegistryAutoConfiguration.class,
    HealthEndpointAutoConfiguration.class,
    InfoEndpointAutoConfiguration.class,
    SslAutoConfiguration.class,
    ConfigurationPropertiesRebinderAutoConfiguration.class
})
@Import(ServletWebServerConfiguration.class)
@Inherited
@EnableScheduling
@EnableResilientMethods
@EnableTransactionManagement
@EnableConfigurationProperties({
    ManagementServerProperties.class,
    WebEndpointProperties.class,
    WebProperties.class,
    ServerProperties.class,
    MailProperties.class,
    SecurityProperties.class,
    TomcatServerProperties.class,
    CasConfigurationProperties.class
})
public @interface SpringBootTestAutoConfigurations {
}
