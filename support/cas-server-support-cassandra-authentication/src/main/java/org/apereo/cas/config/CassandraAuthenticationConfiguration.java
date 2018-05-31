package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CassandraAuthenticationHandler;
import org.apereo.cas.authentication.CassandraRepository;
import org.apereo.cas.authentication.DefaultCassandraRepository;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CassandraAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Configuration("cassandraAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CassandraAuthenticationConfiguration {

    @Autowired
    @Qualifier("cassandraSessionFactory")
    private CassandraSessionFactory cassandraSessionFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public PrincipalFactory cassandraPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public CassandraRepository cassandraRepository() {
        final var cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraRepository(cassandra, cassandraSessionFactory);
    }
    
    @Bean
    public AuthenticationHandler cassandraAuthenticationHandler() {
        final var cassandra = casProperties.getAuthn().getCassandra();
        final var handler = new CassandraAuthenticationHandler(cassandra.getName(), servicesManager,
                cassandraPrincipalFactory(),
                cassandra.getOrder(), cassandra, cassandraRepository());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cassandra.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cassandra.getPasswordEncoder()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cassandraAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer cassandraAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cassandraAuthenticationHandler(), personDirectoryPrincipalResolver);
    }
}
