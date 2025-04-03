package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasCommandLineShellApplication}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    GroovyTemplateAutoConfiguration.class
}, proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync(proxyTargetClass = false)
@NoArgsConstructor
public class CasCommandLineShellApplication {

    /**
     * Main entry point of the CAS shell.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        val applicationContext = new SpringApplicationBuilder(CasCommandLineShellApplication.class)
            .banner(new DefaultCasBanner())
            .bannerMode(Banner.Mode.CONSOLE)
            .logStartupInfo(true)
            .web(WebApplicationType.NONE)
            .run(args);
        System.exit(SpringApplication.exit(applicationContext));
    }
}
