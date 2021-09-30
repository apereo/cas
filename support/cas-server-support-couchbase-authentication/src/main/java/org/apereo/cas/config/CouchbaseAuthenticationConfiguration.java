package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CouchbaseAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CouchbaseAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "couchbaseAuthenticationConfiguration", proxyBeanMethods = false)
public class CouchbaseAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "couchbasePrincipalFactory")
    @Bean
    public PrincipalFactory couchbasePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationCouchbaseClientFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public CouchbaseClientFactory authenticationCouchbaseClientFactory(final CasConfigurationProperties casProperties) {
        val couchbase = casProperties.getAuthn().getCouchbase();
        return new CouchbaseClientFactory(couchbase);
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler couchbaseAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                @Qualifier("couchbasePrincipalFactory")
                                                                final PrincipalFactory couchbasePrincipalFactory,
                                                                @Qualifier("authenticationCouchbaseClientFactory")
                                                                final CouchbaseClientFactory authenticationCouchbaseClientFactory,
                                                                @Qualifier(ServicesManager.BEAN_NAME)
                                                                final ServicesManager servicesManager) {
        val couchbase = casProperties.getAuthn().getCouchbase();
        val handler = new CouchbaseAuthenticationHandler(servicesManager, couchbasePrincipalFactory, authenticationCouchbaseClientFactory, couchbase);
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(couchbase.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(couchbase.getPasswordEncoder(), applicationContext));
        return handler;
    }

    @ConditionalOnMissingBean(name = "couchbaseAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer couchbaseAuthenticationEventExecutionPlanConfigurer(final CasConfigurationProperties casProperties,
                                                                                                          @Qualifier("couchbaseAuthenticationHandler")
                                                                                                          final AuthenticationHandler couchbaseAuthenticationHandler,
                                                                                                          @Qualifier("defaultPrincipalResolver")
                                                                                                          final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val couchbase = casProperties.getAuthn().getCouchbase();
            if (StringUtils.isNotBlank(couchbase.getPasswordAttribute()) && StringUtils.isNotBlank(couchbase.getUsernameAttribute())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(couchbaseAuthenticationHandler, defaultPrincipalResolver);
            } else {
                LOGGER.debug("No couchbase username/password is defined, so couchbase authentication will not be registered in the execution plan");
            }
        };
    }
}
