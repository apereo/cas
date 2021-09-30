package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.syncope.authentication.SyncopeAuthenticationHandler;

import lombok.val;
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
 * This is {@link SyncopeAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "syncopeAuthenticationConfiguration", proxyBeanMethods = false)
public class SyncopeAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "syncopePrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory syncopePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationHandler")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationHandler syncopeAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                              @Qualifier("syncopePrincipalFactory")
                                                              final PrincipalFactory syncopePrincipalFactory,
                                                              @Qualifier("syncopePasswordPolicyConfiguration")
                                                              final PasswordPolicyContext syncopePasswordPolicyConfiguration,
                                                              @Qualifier(ServicesManager.BEAN_NAME)
                                                              final ServicesManager servicesManager) {
        val syncope = casProperties.getAuthn().getSyncope();
        val h = new SyncopeAuthenticationHandler(syncope.getName(), servicesManager, syncopePrincipalFactory, syncope.getUrl(), syncope.getDomain());
        h.setState(syncope.getState());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(syncope.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(syncopePasswordPolicyConfiguration);
        h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(syncope.getCredentialCriteria()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(syncope.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer syncopeAuthenticationEventExecutionPlanConfigurer(final CasConfigurationProperties casProperties,
                                                                                                        @Qualifier("syncopeAuthenticationHandler")
                                                                                                        final AuthenticationHandler syncopeAuthenticationHandler,
                                                                                                        @Qualifier("defaultPrincipalResolver")
                                                                                                        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val syncope = casProperties.getAuthn().getSyncope();
            if (syncope.isDefined()) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(syncopeAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }

    @ConditionalOnMissingBean(name = "syncopePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext syncopePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
