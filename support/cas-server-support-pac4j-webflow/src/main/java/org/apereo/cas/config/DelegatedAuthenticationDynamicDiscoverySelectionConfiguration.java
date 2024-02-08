package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.pac4j.discovery.DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationDynamicDiscoveryExecutionAction;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link DelegatedAuthenticationDynamicDiscoverySelectionConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "dynamic-discovery")
@Configuration(value = "DelegatedAuthenticationDynamicDiscoverySelectionConfiguration", proxyBeanMethods = false)
class DelegatedAuthenticationDynamicDiscoverySelectionConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.pac4j.core.discovery-selection.selection-type").havingValue("DYNAMIC");
    private static final BeanCondition CONDITION_JSON = BeanCondition.on("cas.authn.pac4j.core.discovery-selection.json.location").exists();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedAuthenticationDynamicDiscoveryProviderLocator")
    public DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator(
        @Qualifier("clientPrincipalFactory")
        final PrincipalFactory clientPrincipalFactory,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(DelegatedAuthenticationDynamicDiscoveryProviderLocator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .and(CONDITION_JSON.given(applicationContext.getEnvironment()))
            .supply(() -> new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(
                configContext.getDelegatedClientIdentityProvidersProducer(), configContext.getIdentityProviders(),
                defaultPrincipalResolver, clientPrincipalFactory, casProperties))
            .otherwiseProxy()
            .get();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
    @Bean
    public Action delegatedAuthenticationProviderDynamicDiscoveryExecutionAction(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        @Qualifier("delegatedAuthenticationDynamicDiscoveryProviderLocator")
        final DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator) {
        return WebflowActionBeanSupplier.builder()
            .withApplicationContext(applicationContext)
            .withProperties(casProperties)
            .withAction(() -> BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new DelegatedClientAuthenticationDynamicDiscoveryExecutionAction(
                    configContext, delegatedAuthenticationDynamicDiscoveryProviderLocator))
                .otherwise(() -> ConsumerExecutionAction.NONE)
                .get())
            .withId(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
            .build()
            .get();
    }
}
