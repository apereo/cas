package org.apereo.cas.metadata.server;

import org.apereo.cas.config.CasCoreMetadataServerConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link CasConfigurationMetadataServerApplication}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SpringBootApplication(
        exclude = {
                CasCoreMetadataServerConfiguration.class,
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
public class CasConfigurationMetadataServerApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationMetadataServerApplication.class);

    protected CasConfigurationMetadataServerApplication() {}
    
    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        SpringApplication.run(CasConfigurationMetadataServerApplication.class, args);
    }

    /**
     * Command line runner command line runner.
     *
     * @return the command line runner
     * @throws Exception the exception
     */
    @Bean
    public CommandLineRunner commandLineRunner() throws Exception {
        return args -> {
            final CasConfigurationMetadataRepository repository = new CasConfigurationMetadataRepository();
            System.out.println(repository.getRepository().getAllGroups().size());
        };
    }
}
