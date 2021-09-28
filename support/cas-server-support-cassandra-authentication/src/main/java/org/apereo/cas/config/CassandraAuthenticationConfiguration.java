package org.apereo.cas.config;

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
import org.apereo.cas.cassandra.DefaultCassandraSessionFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

/**
 * This is {@link CassandraAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "cassandraAuthenticationConfiguration", proxyBeanMethods = false)
public class CassandraAuthenticationConfiguration {

    @Bean
    @RefreshScope
    public PrincipalFactory cassandraPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "cassandraAuthnSessionFactory")
    @Autowired
    public CassandraSessionFactory cassandraAuthnSessionFactory(final CasConfigurationProperties casProperties,
                                                                @Qualifier("sslContext")
                                                                final SSLContext sslContext) {
        val cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, sslContext);
    }

    @Bean
    @RefreshScope
    @Autowired
    public CassandraRepository cassandraRepository(final CasConfigurationProperties casProperties,
                                                   @Qualifier("cassandraAuthnSessionFactory")
                                                   final CassandraSessionFactory cassandraAuthnSessionFactory) {
        val cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraRepository(cassandra, cassandraAuthnSessionFactory);
    }

    @Bean
    @Autowired
    public AuthenticationHandler cassandraAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                @Qualifier("cassandraPrincipalFactory")
                                                                final PrincipalFactory cassandraPrincipalFactory,
                                                                @Qualifier("cassandraRepository")
                                                                final CassandraRepository cassandraRepository,
                                                                @Qualifier(ServicesManager.BEAN_NAME)
                                                                final ServicesManager servicesManager) {
        val cassandra = casProperties.getAuthn().getCassandra();
        val handler = new CassandraAuthenticationHandler(cassandra.getName(), servicesManager, cassandraPrincipalFactory, cassandra.getOrder(), cassandra, cassandraRepository);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cassandra.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cassandra.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cassandraAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer cassandraAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("cassandraAuthenticationHandler")
        final AuthenticationHandler cassandraAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cassandraAuthenticationHandler, defaultPrincipalResolver);
    }
}
