package org.apereo.cas.web;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Instant;

/**
 * This is {@link CasWebApplication} that houses the main method.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@NoArgsConstructor
@Slf4j
public class CasWebApplication {

    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        CasEmbeddedContainerUtils.getLoggingInitialization()
            .ifPresent(init -> init.setMainArguments(args));
        val banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        new SpringApplicationBuilder(CasWebApplication.class)
            .banner(banner)
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .contextFactory(webApplicationType -> new CasWebApplicationContext())
            .applicationStartup(CasEmbeddedContainerUtils.getApplicationStartup())
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
