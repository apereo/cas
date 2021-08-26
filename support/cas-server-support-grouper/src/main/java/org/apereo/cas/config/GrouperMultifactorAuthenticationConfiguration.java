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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link GrouperMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("grouperMultifactorAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnProperty(name = "cas.authn.mfa.triggers.grouper.grouper-group-field")
public class GrouperMultifactorAuthenticationConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("multifactorAuthenticationProviderResolver")
    private ObjectProvider<MultifactorAuthenticationProviderResolver> multifactorAuthenticationProviderResolver;

    @Autowired
    @Qualifier("casWebflowConfigurationContext")
    private ObjectProvider<CasWebflowEventResolutionConfigurationContext> casWebflowConfigurationContext;

    @Bean
    @ConditionalOnMissingBean(name = "grouperFacade")
    public GrouperFacade grouperFacade() {
        return new GrouperFacade();
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationTrigger grouperMultifactorAuthenticationTrigger() {
        return new GrouperMultifactorAuthenticationTrigger(casProperties,
            multifactorAuthenticationProviderResolver.getObject(), grouperFacade(),
            this.applicationContext);
    }

    @Bean
    @RefreshScope
    public CasWebflowEventResolver grouperMultifactorAuthenticationWebflowEventResolver() {
        val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
            casWebflowConfigurationContext.getObject(),
            grouperMultifactorAuthenticationTrigger());
        LOGGER.debug("Activating MFA event resolver based on Grouper groups...");
        this.initialAuthenticationAttemptWebflowEventResolver.getObject().addDelegate(r);
        return r;
    }
}
