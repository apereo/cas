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
import org.apereo.cas.syncope.SyncopeAuthenticationHandler;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;

import com.google.common.base.Splitter;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.stream.Collectors;

/**
 * This is {@link SyncopeAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "SyncopeAuthenticationConfiguration", proxyBeanMethods = false)
public class SyncopeAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "syncopePrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory syncopePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationHandlers")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("syncopePrincipalFactory")
        final PrincipalFactory syncopePrincipalFactory,
        @Qualifier("syncopePasswordPolicyConfiguration")
        final PasswordPolicyContext syncopePasswordPolicyConfiguration,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {

        val syncope = casProperties.getAuthn().getSyncope();
        val handlers = Splitter.on(",").splitToList(syncope.getDomain())
            .stream()
            .map(domain -> {
                val h = new SyncopeAuthenticationHandler(syncope.getName(), servicesManager,
                    syncopePrincipalFactory, syncope.getUrl(), domain.trim());
                h.setState(syncope.getState());
                h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(syncope.getPasswordEncoder(), applicationContext));
                h.setPasswordPolicyConfiguration(syncopePasswordPolicyConfiguration);
                val predicate = CoreAuthenticationUtils.newCredentialSelectionPredicate(syncope.getCredentialCriteria());
                h.setCredentialSelectionPredicate(predicate);
                val transformer = PrincipalNameTransformerUtils.newPrincipalNameTransformer(syncope.getPrincipalTransformation());
                h.setPrincipalNameTransformer(transformer);
                return h;
            })
            .map(AuthenticationHandler.class::cast)
            .collect(Collectors.toList());
        return BeanContainer.of(handlers);
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer syncopeAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("syncopeAuthenticationHandlers")
        final BeanContainer<AuthenticationHandler> syncopeAuthenticationHandlers,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val syncope = casProperties.getAuthn().getSyncope();
            FunctionUtils.doIf(syncope.isDefined(),
                    o -> syncopeAuthenticationHandlers.toList().forEach(
                        handler -> plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver)))
                .accept(syncope);
        };
    }

    @ConditionalOnMissingBean(name = "syncopePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext syncopePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
