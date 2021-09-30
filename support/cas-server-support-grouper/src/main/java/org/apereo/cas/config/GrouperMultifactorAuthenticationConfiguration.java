package org.apereo.cas.config;

import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationTrigger;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
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
 * This is {@link GrouperMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnProperty(name = "cas.authn.mfa.triggers.grouper.grouper-group-field")
@Configuration(value = "grouperMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
public class GrouperMultifactorAuthenticationConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "grouperFacade")
    public GrouperFacade grouperFacade() {
        return new GrouperFacade();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationTrigger grouperMultifactorAuthenticationTrigger(final CasConfigurationProperties casProperties,
                                                                                    final ConfigurableApplicationContext applicationContext,
                                                                                    @Qualifier("grouperFacade")
                                                                                    final GrouperFacade grouperFacade,
                                                                                    @Qualifier("multifactorAuthenticationProviderResolver")
                                                                                    final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return new GrouperMultifactorAuthenticationTrigger(casProperties, multifactorAuthenticationProviderResolver, grouperFacade, applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowEventResolver grouperMultifactorAuthenticationWebflowEventResolver(
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("grouperMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger grouperMultifactorAuthenticationTrigger,
        @Qualifier("casWebflowConfigurationContext")
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(casWebflowConfigurationContext, grouperMultifactorAuthenticationTrigger);
        LOGGER.debug("Activating MFA event resolver based on Grouper groups...");
        initialAuthenticationAttemptWebflowEventResolver.addDelegate(r);
        return r;
    }
}
