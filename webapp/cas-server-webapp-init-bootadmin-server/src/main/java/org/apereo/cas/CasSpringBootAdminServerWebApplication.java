package org.apereo.cas;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import lombok.NoArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * This is {@link CasSpringBootAdminServerWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootApplication(exclude = {
    GroovyTemplateAutoConfiguration.class,
    DataSourceAutoConfiguration.class
}, proxyBeanMethods = false)
@EnableAdminServer
@NoArgsConstructor
public class CasSpringBootAdminServerWebApplication {

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasSpringBootAdminServerWebApplication.class)
            .banner(new CasSpringBootAdminServerBanner())
            .bannerMode(Banner.Mode.CONSOLE)
            .logStartupInfo(true)
            .web(WebApplicationType.SERVLET)
            .run(args);
    }
}
