package org.apereo.cas.config;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.passwordless.DuoSecurityPasswordlessUserAccountStore;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityVerifyPasswordlessAuthenticationAction;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnFeaturesEnabled;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;
import java.util.List;

/**
 * This is {@link DuoSecurityPasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeaturesEnabled({
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn),
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.MultifactorAuthentication, module = "duo")
})
@ConditionalOnClass(PasswordlessUserAccountStore.class)
@Configuration(value = "DuoSecurityPasswordlessAuthenticationConfiguration", proxyBeanMethods = false)
class DuoSecurityPasswordlessAuthenticationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "duoSecurityPasswordlessUserAccountStore")
    public BeanSupplier<PasswordlessUserAccountStore> duoSecurityPasswordlessUserAccountStore(
        final List<PasswordlessUserAccountCustomizer> customizerList,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(PasswordlessUserAccountStore.class)
            .alwaysMatch()
            .supply(() -> new DuoSecurityPasswordlessUserAccountStore(applicationContext, customizerList));
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DUO_PASSWORDLESS_VERIFY)
    public Action duoSecurityVerifyPasswordlessAuthenticationAction(
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport,
        @Qualifier("duoAuthenticationWebflowEventResolver")
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> BeanSupplier.of(Action.class)
                .when(DuoSecurityAuthenticationService.CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DuoSecurityVerifyPasswordlessAuthenticationAction(authenticationSystemSupport, duoAuthenticationWebflowEventResolver))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get())
            .withId(CasWebflowConstants.ACTION_ID_DUO_PASSWORDLESS_VERIFY)
            .build()
            .get();
    }
}
