package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.okta.OktaAuthenticationHandler;
import org.apereo.cas.okta.OktaConfigurationFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import com.okta.authn.sdk.client.AuthenticationClient;
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
 * This is {@link OktaAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "okta")
@Configuration(value = "OktaAuthenticationConfiguration", proxyBeanMethods = false)
class OktaAuthenticationConfiguration {

    @Configuration(value = "OktaAuthenticationCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OktaAuthenticationCoreConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.okta.organization-url");

        @ConditionalOnMissingBean(name = "oktaAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer oktaAuthenticationEventExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
            final PrincipalResolver defaultPrincipalResolver,
            @Qualifier("oktaAuthenticationHandler")
            final AuthenticationHandler oktaAuthenticationHandler) {
            return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
                    oktaAuthenticationHandler, defaultPrincipalResolver))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oktaPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory oktaPrincipalFactory(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(PrincipalFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(PrincipalFactoryUtils::newPrincipalFactory)
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oktaAuthenticationHandler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationHandler oktaAuthenticationHandler(
            @Qualifier("oktaPrincipalFactory")
            final PrincipalFactory oktaPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("oktaAuthenticationClient")
            final AuthenticationClient oktaAuthenticationClient,
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuthenticationHandler.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val okta = casProperties.getAuthn().getOkta();
                    val handler = new OktaAuthenticationHandler(okta.getName(),
                        oktaPrincipalFactory, okta, oktaAuthenticationClient);
                    handler.setState(okta.getState());
                    handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(okta.getPrincipalTransformation()));
                    handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(okta.getPasswordEncoder(), applicationContext));
                    handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(okta.getCredentialCriteria()));
                    return handler;
                })
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oktaAuthenticationClient")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationClient oktaAuthenticationClient(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(AuthenticationClient.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val properties = casProperties.getAuthn().getOkta();
                    return OktaConfigurationFactory.buildAuthenticationClient(properties);
                })
                .otherwiseProxy()
                .get();
        }
    }
}
