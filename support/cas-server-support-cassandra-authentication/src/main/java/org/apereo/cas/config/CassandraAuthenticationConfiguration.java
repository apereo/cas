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
import org.springframework.beans.factory.ObjectProvider;
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
@Configuration("cassandraAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CassandraAuthenticationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("sslContext")
    private ObjectProvider<SSLContext> sslContext;

    @Bean
    @RefreshScope
    public PrincipalFactory cassandraPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "cassandraAuthnSessionFactory")
    public CassandraSessionFactory cassandraAuthnSessionFactory() {
        val cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraSessionFactory(cassandra, sslContext.getObject());
    }

    @Bean
    @RefreshScope
    public CassandraRepository cassandraRepository() {
        val cassandra = casProperties.getAuthn().getCassandra();
        return new DefaultCassandraRepository(cassandra, cassandraAuthnSessionFactory());
    }

    @Bean
    public AuthenticationHandler cassandraAuthenticationHandler() {
        val cassandra = casProperties.getAuthn().getCassandra();
        val handler = new CassandraAuthenticationHandler(cassandra.getName(), servicesManager.getObject(),
            cassandraPrincipalFactory(),
            cassandra.getOrder(), cassandra, cassandraRepository());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(cassandra.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(cassandra.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "cassandraAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer cassandraAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(cassandraAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }
}
