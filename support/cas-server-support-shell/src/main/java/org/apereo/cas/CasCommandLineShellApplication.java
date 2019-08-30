package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.NoArgsConstructor;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
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
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasCommandLineShellApplication}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    JerseyAutoConfiguration.class,
    GroovyTemplateAutoConfiguration.class,
    JmxAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    RedisAutoConfiguration.class,
    MongoAutoConfiguration.class,
    LogbackMetricsAutoConfiguration.class,
    MongoDataAutoConfiguration.class,
    CassandraAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    BatchAutoConfiguration.class,
    TaskExecutionAutoConfiguration.class,
    QuartzAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync
@NoArgsConstructor
public class CasCommandLineShellApplication {

    /**
     * Main entry point of the CAS shell.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasCommandLineShellApplication.class)
            .banner(new DefaultCasBanner())
            .bannerMode(Banner.Mode.CONSOLE)
            .logStartupInfo(true)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
