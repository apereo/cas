package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.MongoDbAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link CasMongoAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "mongo")
@AutoConfiguration
public class CasMongoAuthenticationAutoConfiguration {

    @ConditionalOnMissingBean(name = "mongoPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory mongoPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "mongoAuthenticationHandler")
    public AuthenticationHandler mongoAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("mongoPrincipalFactory")
        final PrincipalFactory mongoPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext) {
        val mongo = casProperties.getAuthn().getMongo();
        val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
        val mongoTemplate = factory.buildMongoTemplate(mongo);
        val handler = new MongoDbAuthenticationHandler(mongo.getName(), mongoPrincipalFactory, mongo, mongoTemplate);
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(mongo.getPasswordEncoder(), applicationContext));
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(mongo.getPrincipalTransformation()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "mongoAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer mongoAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("mongoAuthenticationHandler")
        final AuthenticationHandler mongoAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(mongoAuthenticationHandler, defaultPrincipalResolver);
    }
}
