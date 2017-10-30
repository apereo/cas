package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.shell.CasCommandLineShellBootstrapper;
import org.apereo.cas.shell.cli.CasCommandLineEngine;
import org.apereo.cas.shell.cli.CasCommandLineParser;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.MetricsDropwizardAutoConfiguration;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * This is {@link CasCommandLineShellApplication}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@SpringBootApplication(
        exclude = {
                HibernateJpaAutoConfiguration.class,
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAsync
public class CasCommandLineShellApplication {
    protected CasCommandLineShellApplication() {
    }

    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        new SpringApplicationBuilder(CasCommandLineShellApplication.class)
                .banner(new DefaultCasBanner())
                .bannerMode(CasCommandLineParser.getBannerMode(args))
                .logStartupInfo(false)
                .web(false)
                .run(args);
    }

    /**
     * Command line runner.
     *
     * @return the command line runner
     */
    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            if (CasCommandLineParser.isShell(args)) {
                final CasCommandLineShellBootstrapper sh = new CasCommandLineShellBootstrapper();
                sh.execute(args);
            } else {
                final CasCommandLineEngine engine = new CasCommandLineEngine();
                engine.execute(args);
            }
        };
    }
}
