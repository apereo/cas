package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import lombok.NoArgsConstructor;

/**
 * This is {@link CasEurekaServerWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = { HibernateJpaAutoConfiguration.class, JerseyAutoConfiguration.class,
    GroovyTemplateAutoConfiguration.class, JmxAutoConfiguration.class, DataSourceAutoConfiguration.class, RedisAutoConfiguration.class,
    MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, CassandraAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class, MetricsDropwizardAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class })
@EnableEurekaServer
@Slf4j
@NoArgsConstructor
public class CasEurekaServerWebApplication {

    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasEurekaServerWebApplication.class).banner(new CasEurekaServerBanner()).logStartupInfo(true).run(args);
    }
}
