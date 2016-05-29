package org.apereo.cas.console.groovy.config;

import org.apereo.cas.console.groovy.GroovyShellService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasGroovyConsoleConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casGroovyConsoleConfiguration")
public class CasGroovyConsoleConfiguration {

    @Bean
    @RefreshScope
    public GroovyShellService groovyShellService() {
        return new GroovyShellService();
    }
}
