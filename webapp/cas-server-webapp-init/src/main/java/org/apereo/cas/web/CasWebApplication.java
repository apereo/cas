package org.apereo.cas.web;

import org.apereo.cas.config.CasEmbeddedContainerConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.CasBanner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Collections;

/**
 * This is {@link CasWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ImportResource(locations = {
        "classpath:/deployerConfigContext.groovy",
        "classpath:/deployerConfigContext.xml"})
@SpringBootConfiguration
@EnableAutoConfiguration(
        exclude = {HibernateJpaAutoConfiguration.class,
                JerseyAutoConfiguration.class,
                GroovyTemplateAutoConfiguration.class,
                JmxAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                RedisAutoConfiguration.class,
                MongoAutoConfiguration.class,
                MongoDataAutoConfiguration.class,
                CassandraAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                MetricsDropwizardAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class})
@EnableConfigServer
@EnableAsync
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableScheduling
public class CasWebApplication {
    /**
     * Instantiates a new Cas web application.
     */
    protected CasWebApplication() {
    }

    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasWebApplication.class)
                .banner(new CasBanner())
                .properties(Collections.singletonMap(CasEmbeddedContainerConfiguration.EMBEDDED_CONTAINER_CONFIG_ACTIVE, Boolean.TRUE))
                .logStartupInfo(true)
                .run(args);
    }
}
