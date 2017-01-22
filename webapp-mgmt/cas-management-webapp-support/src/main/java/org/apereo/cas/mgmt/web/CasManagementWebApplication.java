package org.apereo.cas.mgmt.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * This is {@link CasManagementWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ImportResource(locations = {"classpath:/managementConfigContext.xml"})
@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {HibernateJpaAutoConfiguration.class,
                JerseyAutoConfiguration.class,
                GroovyTemplateAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                JmxAutoConfiguration.class,
                MetricsDropwizardAutoConfiguration.class})
@Import(value = AopAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigServer
public class CasManagementWebApplication {
    /**
     * Instantiates a new web application.
     */
    protected CasManagementWebApplication() {
    }

    /**
     * Main entry point of the web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasManagementWebApplication.class).banner(new CasManagementBanner()).run(args);
    }
}
