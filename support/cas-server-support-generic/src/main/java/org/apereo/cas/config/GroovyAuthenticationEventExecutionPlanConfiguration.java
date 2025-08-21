package org.apereo.cas.config;

import org.apereo.cas.adaptors.generic.GroovyAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link GroovyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "generic")
@Configuration(value = "GroovyAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
@ConditionalOnMissingGraalVMNativeImage
class GroovyAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "groovyPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingGraalVMNativeImage
    public PrincipalFactory groovyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingGraalVMNativeImage
    public AuthenticationHandler groovyResourceAuthenticationHandler(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("groovyPrincipalFactory")
        final PrincipalFactory groovyPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {

        return BeanSupplier.of(AuthenticationHandler.class)
            .when(BeanCondition.on("cas.authn.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val groovy = casProperties.getAuthn().getGroovy();
                val handler = new GroovyAuthenticationHandler(groovy.getName(),
                    servicesManager, groovyPrincipalFactory, groovy.getLocation(), groovy.getOrder());
                handler.setState(groovy.getState());
                return handler;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "groovyResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingGraalVMNativeImage
    public AuthenticationEventExecutionPlanConfigurer groovyResourceAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("groovyResourceAuthenticationHandler")
        final AuthenticationHandler groovyResourceAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val file = casProperties.getAuthn().getGroovy().getLocation();
            if (file != null) {
                LOGGER.debug("Activating Groovy authentication handler via [{}]", file);
                plan.registerAuthenticationHandlerWithPrincipalResolver(groovyResourceAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }
}
