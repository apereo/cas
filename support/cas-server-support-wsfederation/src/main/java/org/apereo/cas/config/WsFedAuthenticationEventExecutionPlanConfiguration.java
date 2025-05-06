package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegatedCookieProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.attributes.GroovyWsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieCipherExecutor;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
 * This is {@link WsFedAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederation)
@Configuration(value = "WsFedAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class WsFedAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "WsFedAuthenticationProvidersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WsFedAuthenticationProvidersConfiguration {
        private static WsFederationAttributeMutator getAttributeMutatorForWsFederationConfig(final WsFederationDelegationProperties wsfed) {
            val location = wsfed.getAttributeMutatorScript().getLocation();
            val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
            if (location != null && scriptFactory.isPresent()) {
                val watchableScript = scriptFactory.get().fromResource(location);
                return new GroovyWsFederationAttributeMutator(watchableScript);
            }
            return WsFederationAttributeMutator.noOp();
        }

        private static WsFederationConfiguration getWsFederationConfiguration(
            final WsFederationDelegationProperties wsfed,
            final ConfigurableApplicationContext applicationContext) {
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val config = new WsFederationConfiguration();

            config.setAttributesType(WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.valueOf(wsfed.getAttributesType()));
            config.setIdentityAttribute(wsfed.getIdentityAttribute());

            val id = resolver.resolve(wsfed.getIdentityProviderIdentifier());
            config.setIdentityProviderIdentifier(id);

            val url = resolver.resolve(wsfed.getIdentityProviderUrl());
            config.setIdentityProviderUrl(url);

            val rpId = resolver.resolve(wsfed.getRelyingPartyIdentifier());
            config.setRelyingPartyIdentifier(rpId);

            val resources = resolver.resolve(wsfed.getSigningCertificateResources());
            config.setSigningCertificates(resources);

            org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionPrivateKey()).forEach(
                s -> config.setEncryptionPrivateKey(applicationContext.getResource(s)));
            org.springframework.util.StringUtils.commaDelimitedListToSet(wsfed.getEncryptionCertificate()).forEach(
                s -> config.setEncryptionCertificate(applicationContext.getResource(s)));
            config.setEncryptionPrivateKeyPassword(wsfed.getEncryptionPrivateKeyPassword());
            config.setAttributeMutator(getAttributeMutatorForWsFederationConfig(wsfed));
            config.setAutoRedirectType(wsfed.getAutoRedirectType());
            config.setName(wsfed.getName());
            config.setTolerance(Beans.newDuration(wsfed.getTolerance()).toMillis());
            config.setCookieGenerator(getCookieGeneratorForWsFederationConfig(wsfed, applicationContext));
            FunctionUtils.doIfNotNull(wsfed.getId(), config::setId);
            return config;
        }

        private static CasCookieBuilder getCookieGeneratorForWsFederationConfig(final WsFederationDelegationProperties wsfed,
                                                                                final ConfigurableApplicationContext applicationContext) {
            val cookie = wsfed.getCookie();
            val cipher = getCipherExecutorForWsFederationConfig(cookie);
            val geoLocationService = applicationContext.getBeanProvider(GeoLocationService.class);
            val tenantExtractor = applicationContext.getBean(TenantExtractor.BEAN_NAME, TenantExtractor.class);

            val valueManager = new DefaultCasCookieValueManager(
                CipherExecutorResolver.withCipherExecutor(cipher),
                tenantExtractor, geoLocationService,
                DefaultCookieSameSitePolicy.INSTANCE, cookie);
            return new CookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(cookie), valueManager);
        }

        private static CipherExecutor getCipherExecutorForWsFederationConfig(final WsFederationDelegatedCookieProperties cookie) {
            return FunctionUtils.doIf(cookie.getCrypto().isEnabled(),
                () -> CipherExecutorUtils.newStringCipherExecutor(cookie.getCrypto(), WsFederationCookieCipherExecutor.class),
                () -> {
                    LOGGER.info("WsFederation delegated authentication cookie encryption/signing is turned off and "
                        + "MAY NOT be safe in a production environment. "
                        + "Consider using other choices to handle encryption, signing and verification of delegated authentication cookie.");
                    return CipherExecutor.noOp();
                }).get();
        }

        @ConditionalOnMissingBean(name = "wsFederationConfigurations")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<WsFederationConfiguration> wsFederationConfigurations(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val col = casProperties.getAuthn().getWsfed()
                .stream()
                .map(wsfed -> getWsFederationConfiguration(wsfed, applicationContext))
                .collect(Collectors.toSet());
            return BeanContainer.of(col);
        }
    }

    @Configuration(value = "WsFedAuthenticationEventExecutionPlanPrincipalConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WsFedAuthenticationEventExecutionPlanPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "wsfedPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory wsfedPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }


    }

    @Configuration(value = "WsFedAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class WsFedAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "wsfedAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer wsfedAuthenticationEventExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(AttributeDefinitionStore.BEAN_NAME)
            final AttributeDefinitionStore attributeDefinitionStore,
            final CasConfigurationProperties casProperties,
            @Qualifier("wsfedPrincipalFactory")
            final PrincipalFactory wsfedPrincipalFactory,
            @Qualifier("wsFederationConfigurations")
            final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final PersonAttributeDao attributeRepository,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(AttributeRepositoryResolver.BEAN_NAME)
            final AttributeRepositoryResolver attributeRepositoryResolver) {
            val personDirectory = casProperties.getPersonDirectory();
            return plan -> casProperties.getAuthn()
                .getWsfed()
                .stream()
                .filter(wsfed -> StringUtils.isNotBlank(wsfed.getIdentityProviderUrl())
                    && StringUtils.isNotBlank(wsfed.getIdentityProviderIdentifier()))
                .forEach(wsfed -> {
                    val handler = new WsFederationAuthenticationHandler(wsfed.getName(), servicesManager, wsfedPrincipalFactory, wsfed.getOrder());
                    if (wsfed.isAttributeResolverEnabled()) {
                        val cfg = wsFederationConfigurations.toSet().stream()
                            .filter(c -> {
                                val resolver = SpringExpressionLanguageValueResolver.getInstance();
                                return c.getIdentityProviderUrl().equalsIgnoreCase(resolver.resolve(wsfed.getIdentityProviderUrl()));
                            })
                            .findFirst()
                            .orElseThrow(() ->
                                new RuntimeException("Unable to find configuration for identity provider " + wsfed.getIdentityProviderUrl()));
                        val principal = wsfed.getPrincipal();
                        val resolver = PersonDirectoryPrincipalResolver.newPersonDirectoryPrincipalResolver(
                            applicationContext, wsfedPrincipalFactory, attributeRepository,
                            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
                            WsFederationCredentialsToPrincipalResolver.class, servicesManager, attributeDefinitionStore,
                            attributeRepositoryResolver, principal, personDirectory);
                        resolver.setConfiguration(cfg);
                        plan.registerAuthenticationHandlerWithPrincipalResolver(handler, resolver);
                    } else {
                        plan.registerAuthenticationHandler(handler);
                    }
                });
        }
    }

}
