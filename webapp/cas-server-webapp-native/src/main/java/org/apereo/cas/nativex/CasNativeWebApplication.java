package org.apereo.cas.nativex;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.app.ApplicationUtils;
import org.apereo.cas.util.spring.boot.CasBanner;
import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasNativeWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@NoArgsConstructor
public class CasNativeWebApplication {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(final String[] args) {
        val banner = CasBanner.getInstance();
        new SpringApplicationBuilder(CasNativeWebApplication.class)
            .banner(banner)
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .applicationStartup(ApplicationUtils.getApplicationStartup())
            .run(args);
    }
}
