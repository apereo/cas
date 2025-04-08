package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.DefaultSamlIdentityProviderDiscoveryFeedService;
import org.apereo.cas.services.SamlIdentityProviderDiscoveryFeedService;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.SamlIdentityProviderDiscoveryFeedController;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.flow.SamlIdentityProviderDiscoveryWebflowConfigurer;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CasSamlIdentityProviderDiscoveryAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@AutoConfiguration
public class CasSamlIdentityProviderDiscoveryAutoConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryEndpointConfigurer")
    public CasWebSecurityConfigurer<Void> identityProviderDiscoveryEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {
            @Override
            public List<String> getIgnoredEndpoints() {
                return List.of(StringUtils.prependIfMissing(SamlIdentityProviderDiscoveryFeedController.BASE_ENDPOINT_IDP_DISCOVERY, "/"));
            }
        };
    }

    @ConditionalOnMissingBean(name = "identityProviderDiscoveryWebflowConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public CasWebflowConfigurer identityProviderDiscoveryWebflowConfigurer(
        final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_DEFINITION_REGISTRY)
        final FlowDefinitionRegistry flowDefinitionRegistry,
        @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
        final FlowBuilderServices flowBuilderServices) {
        return new SamlIdentityProviderDiscoveryWebflowConfigurer(flowBuilderServices,
            flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryCasWebflowExecutionPlanConfigurer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowExecutionPlanConfigurer identityProviderDiscoveryCasWebflowExecutionPlanConfigurer(
        @Qualifier("identityProviderDiscoveryWebflowConfigurer")
        final CasWebflowConfigurer identityProviderDiscoveryWebflowConfigurer) {
        return plan -> plan.registerWebflowConfigurer(identityProviderDiscoveryWebflowConfigurer);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "identityProviderDiscoveryFeedService")
    public SamlIdentityProviderDiscoveryFeedService identityProviderDiscoveryFeedService(
        final ObjectProvider<List<DelegatedClientIdentityProviderAuthorizer>> delegatedClientAuthorizers,
        @Qualifier("samlIdentityProviderEntityParser")
        final BeanContainer<SamlIdentityProviderEntityParser> samlIdentityProviderEntityParser,
        final CasConfigurationProperties casProperties,
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
        final DelegatedIdentityProviders identityProviders,
        @Qualifier(ArgumentExtractor.BEAN_NAME)
        final ArgumentExtractor argumentExtractor) {

        val authorizers = Optional.ofNullable(delegatedClientAuthorizers.getIfAvailable())
            .orElseGet(ArrayList::new)
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .collect(Collectors.toList());
        return new DefaultSamlIdentityProviderDiscoveryFeedService(casProperties,
            samlIdentityProviderEntityParser.toList(),
            identityProviders, argumentExtractor, authorizers);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public SamlIdentityProviderDiscoveryFeedController identityProviderDiscoveryFeedController(
        final CasConfigurationProperties casProperties,
        @Qualifier("identityProviderDiscoveryFeedService")
        final SamlIdentityProviderDiscoveryFeedService identityProviderDiscoveryFeedService) {
        return new SamlIdentityProviderDiscoveryFeedController(casProperties, identityProviderDiscoveryFeedService);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "samlIdentityProviderEntityParser")
    public BeanContainer<SamlIdentityProviderEntityParser> samlIdentityProviderEntityParser(
        final CasConfigurationProperties casProperties,
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
        final DelegatedIdentityProviders identityProviders) {
        val parsers = new ArrayList<SamlIdentityProviderEntityParser>();

        val resource = casProperties.getAuthn().getPac4j().getSamlDiscovery().getResource();
        resource
            .stream()
            .map(SpringResourceProperties::getLocation)
            .filter(Objects::nonNull)
            .forEach(Unchecked.consumer(res -> parsers.add(new SamlIdentityProviderEntityParser(res))));
        
        parsers.add(new SamlIdentityProviderEntityParser(identityProviders));
        return BeanContainer.of(parsers);
    }
}
