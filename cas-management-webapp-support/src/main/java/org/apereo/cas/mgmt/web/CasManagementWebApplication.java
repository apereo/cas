package org.apereo.cas.mgmt.web;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mgmt.config.CasManagementWebAppConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
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
@ImportResource(locations = {
        "classpath:/managementConfigContext.xml"})
@SpringBootApplication(scanBasePackages = {"org.pac4j.springframework", "org.apereo.cas"},
        exclude = {HibernateJpaAutoConfiguration.class,
                JerseyAutoConfiguration.class,
                GroovyTemplateAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                MetricsDropwizardAutoConfiguration.class,
                VelocityAutoConfiguration.class})
@Import(value = {
        AopAutoConfiguration.class, 
        CasCoreServicesConfiguration.class, 
        CasManagementWebAppConfiguration.class})
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
