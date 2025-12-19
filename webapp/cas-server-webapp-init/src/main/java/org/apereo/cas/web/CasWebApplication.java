package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.app.ApplicationUtils;
import org.apereo.cas.util.spring.boot.CasBanner;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasWebApplication} that houses the main method.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@EnableResilientMethods
@EnableAsync(proxyTargetClass = false)
@NoArgsConstructor
public class CasWebApplication {

    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        val applicationClasses = getApplicationSources(args);
        new SpringApplicationBuilder()
            .sources(applicationClasses.toArray(ArrayUtils.EMPTY_CLASS_ARRAY))
            .banner(CasBanner.getInstance())
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .applicationStartup(ApplicationUtils.getApplicationStartup())
            .run(args);
    }

    protected static List<Class> getApplicationSources(final String[] args) {
        val applicationClasses = new ArrayList<Class>();
        applicationClasses.add(CasWebApplication.class);
        ApplicationUtils.getApplicationEntrypointInitializers()
            .forEach(init -> {
                init.initialize(args);
                applicationClasses.addAll(init.getApplicationSources(args));
            });
        return applicationClasses;
    }
}
