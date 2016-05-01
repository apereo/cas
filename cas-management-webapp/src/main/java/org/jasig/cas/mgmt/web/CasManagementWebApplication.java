package org.jasig.cas.mgmt.web;

import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * This is {@link CasManagementWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SpringBootApplication(scanBasePackages = {"org.jasig.cas.mgmt", "org.jasig.cas.services", 
                                           "org.pac4j.springframework"},
        exclude = {HibernateJpaAutoConfiguration.class,
                JerseyAutoConfiguration.class,
                GroovyTemplateAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                MetricsDropwizardAutoConfiguration.class,
                VelocityAutoConfiguration.class})
@ImportResource(locations = {"classpath:/managementConfigContext.xml"})
@Import(AopAutoConfiguration.class)
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
        new SpringApplicationBuilder(CasManagementWebApplication.class).run(args);
    }
}
