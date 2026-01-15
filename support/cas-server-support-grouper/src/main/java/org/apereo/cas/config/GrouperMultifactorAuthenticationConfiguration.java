package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.grouper.DefaultGrouperFacade;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationTrigger;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.mfa.DefaultMultifactorAuthenticationProviderWebflowEventResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link GrouperMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "grouper")
@Configuration(value = "GrouperMultifactorAuthenticationConfiguration", proxyBeanMethods = false)
class GrouperMultifactorAuthenticationConfiguration {

    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.mfa.triggers.grouper.grouper-group-field");

    @Bean
    @ConditionalOnMissingBean(name = "grouperFacade")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public GrouperFacade grouperFacade(final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(GrouperFacade.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(DefaultGrouperFacade::new)
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "grouperMultifactorAuthenticationTrigger")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationTrigger grouperMultifactorAuthenticationTrigger(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("grouperFacade")
        final GrouperFacade grouperFacade,
        @Qualifier(MultifactorAuthenticationProviderResolver.BEAN_NAME)
        final MultifactorAuthenticationProviderResolver multifactorAuthenticationProviderResolver) {
        return BeanSupplier.of(MultifactorAuthenticationTrigger.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new GrouperMultifactorAuthenticationTrigger(casProperties,
                multifactorAuthenticationProviderResolver, grouperFacade, applicationContext))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "grouperMultifactorAuthenticationWebflowEventResolver")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public CasWebflowEventResolver grouperMultifactorAuthenticationWebflowEventResolver(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasDelegatingWebflowEventResolver.BEAN_NAME_INITIAL_AUTHENTICATION_EVENT_RESOLVER)
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver,
        @Qualifier("grouperMultifactorAuthenticationTrigger")
        final MultifactorAuthenticationTrigger grouperMultifactorAuthenticationTrigger,
        @Qualifier(CasWebflowEventResolutionConfigurationContext.BEAN_NAME)
        final CasWebflowEventResolutionConfigurationContext casWebflowConfigurationContext) {
        return BeanSupplier.of(CasWebflowEventResolver.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val r = new DefaultMultifactorAuthenticationProviderWebflowEventResolver(
                    casWebflowConfigurationContext, grouperMultifactorAuthenticationTrigger);
                LOGGER.debug("Activating MFA event resolver based on Grouper groups...");
                initialAuthenticationAttemptWebflowEventResolver.addDelegate(r);
                return r;
            })
            .otherwiseProxy()
            .get();
    }
}
