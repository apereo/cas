package org.apereo.cas.web.flow.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.discovery.DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProviderLocator;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationDynamicDiscoveryExecutionAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
@ConditionalOnProperty(prefix = "cas.authn.pac4j.core.discovery-selection", name = "selection-type", havingValue = "DYNAMIC")
@Configuration(value = "DelegatedAuthenticationDynamicDiscoverySelectionConfiguration", proxyBeanMethods = false)
public class DelegatedAuthenticationDynamicDiscoverySelectionConfiguration {


    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedAuthenticationDynamicDiscoveryProviderLocator")
    @Autowired
    public DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator(
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        final CasConfigurationProperties casProperties) {
        return new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(configContext.getDelegatedClientIdentityProvidersProducer(), configContext.getClients(), casProperties);
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION)
    @Bean
    public Action delegatedAuthenticationProviderDynamicDiscoveryExecutionAction(
        @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
        final DelegatedClientAuthenticationConfigurationContext configContext,
        @Qualifier("delegatedAuthenticationDynamicDiscoveryProviderLocator")
        final DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator) {
        return new DelegatedClientAuthenticationDynamicDiscoveryExecutionAction(configContext, delegatedAuthenticationDynamicDiscoveryProviderLocator);
    }
}
