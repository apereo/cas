package org.apereo.cas;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.event.EventListener;

import java.time.Instant;

/**
 * This is {@link CasEurekaServerWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootApplication(exclude = {
    GroovyTemplateAutoConfiguration.class,
    DataSourceAutoConfiguration.class
}, proxyBeanMethods = false)
@EnableEurekaServer
@NoArgsConstructor
@Slf4j
public class CasEurekaServerWebApplication {

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasEurekaServerWebApplication.class)
            .banner(new CasEurekaServerBanner())
            .logStartupInfo(true)
            .bannerMode(Banner.Mode.CONSOLE)
            .web(WebApplicationType.SERVLET)
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
}
