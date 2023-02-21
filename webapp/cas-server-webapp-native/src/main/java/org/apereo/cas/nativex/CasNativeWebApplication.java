package org.apereo.cas.nativex;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.CasWebApplication;

import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is {@link CasNativeWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication(proxyBeanMethods = false, exclude = {
    HibernateJpaAutoConfiguration.class,
    GroovyTemplateAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    MongoAutoConfiguration.class,
    MongoDataAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableTransactionManagement(proxyTargetClass = false)
@EnableScheduling
@NoArgsConstructor
@ImportRuntimeHints(CasNativeWebAppRuntimeHints.class)
public class CasNativeWebApplication {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(final String[] args) {
        val banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        new SpringApplicationBuilder(CasWebApplication.class)
            .banner(banner)
            .web(WebApplicationType.SERVLET)
            .logStartupInfo(true)
            .applicationStartup(CasEmbeddedContainerUtils.getApplicationStartup())
            .run(args);
    }
}
