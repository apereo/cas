package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.NoArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
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
@SpringBootApplication(exclude = GroovyTemplateAutoConfiguration.class, proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync
@NoArgsConstructor
public class CasCommandLineShellApplication {

    /**
     * Main entry point of the CAS shell.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasCommandLineShellApplication.class)
            .banner(new DefaultCasBanner())
            .bannerMode(Banner.Mode.CONSOLE)
            .logStartupInfo(true)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
