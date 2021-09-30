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
import org.apereo.cas.okta.OktaAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

import com.okta.authn.sdk.client.AuthenticationClient;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Configuration(value = "oktaAuthenticationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty("cas.authn.okta.organization-url")
public class OktaAuthenticationConfiguration {
    @ConditionalOnMissingBean(name = "oktaAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer oktaAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver,
        @Qualifier("oktaAuthenticationHandler")
        final AuthenticationHandler oktaAuthenticationHandler) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
            oktaAuthenticationHandler, defaultPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "oktaPrincipalFactory")
    @Bean
    public PrincipalFactory oktaPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "oktaAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler oktaAuthenticationHandler(
        @Qualifier("oktaPrincipalFactory")
        final PrincipalFactory oktaPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier("oktaAuthenticationClient")
        final AuthenticationClient oktaAuthenticationClient,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val okta = casProperties.getAuthn().getOkta();
        val handler = new OktaAuthenticationHandler(okta.getName(), servicesManager,
            oktaPrincipalFactory, okta, oktaAuthenticationClient);
        handler.setState(okta.getState());
        handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(okta.getPrincipalTransformation()));
        handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(okta.getPasswordEncoder(), applicationContext));
        handler.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(okta.getCredentialCriteria()));
        return handler;
    }

    @ConditionalOnMissingBean(name = "oktaAuthenticationClient")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationClient oktaAuthenticationClient(final CasConfigurationProperties casProperties) {
        val properties = casProperties.getAuthn().getOkta();
        return OktaConfigurationFactory.buildAuthenticationClient(properties);
    }
}
