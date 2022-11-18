package org.apereo.cas;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;

/**
 * This is {@link CasConfigurationServerWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootApplication(exclude = {
    GroovyTemplateAutoConfiguration.class,
    DataSourceAutoConfiguration.class
}, proxyBeanMethods = false)
@EnableConfigServer
@NoArgsConstructor
@Slf4j
public class CasConfigurationServerWebApplication {

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasConfigurationServerWebApplication.class)
            .banner(new CasConfigurationServerBanner())
            .bannerMode(Banner.Mode.CONSOLE)
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .run(args);
    }

    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtReady(LOGGER, StringUtils.EMPTY);
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp())));
    }

    /**
     * CAS configuration server web security configurer adapter.
     *
     * @param serverProperties the server properties
     * @return the web security configurer adapter
     */
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SecurityFilterChain casConfigurationServerWebSecurityConfigurerAdapter(
        final HttpSecurity http,
        final ServerProperties serverProperties) throws Exception {
        val path = serverProperties.getServlet().getContextPath();
        http.authorizeHttpRequests().requestMatchers(path + "/decrypt/**",
            path + "/encrypt/**").authenticated().and().csrf().disable();
        return http.build();
    }
}
