package org.apereo.cas.support.wsfederation.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegatedCookieProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationDelegationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.attributes.GroovyWsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandler;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieCipherExecutor;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;
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
@Configuration(value = "WsfedAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class WsFedAuthenticationEventExecutionPlanConfiguration {

    @Configuration(value = "WsFedAuthenticationProvidersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WsFedAuthenticationProvidersConfiguration {
        private static WsFederationAttributeMutator getAttributeMutatorForWsFederationConfig(final WsFederationDelegationProperties wsfed) {
            val location = wsfed.getAttributeMutatorScript().getLocation();
            if (location != null) {
                return new GroovyWsFederationAttributeMutator(location);
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
            config.setCookieGenerator(getCookieGeneratorForWsFederationConfig(wsfed));
            FunctionUtils.doIfNotNull(wsfed.getId(), config::setId);
            return config;
        }

        private static CasCookieBuilder getCookieGeneratorForWsFederationConfig(final WsFederationDelegationProperties wsfed) {
            val cookie = wsfed.getCookie();
            val cipher = getCipherExecutorForWsFederationConfig(cookie);
            return new WsFederationCookieGenerator(new DefaultCasCookieValueManager(cipher, cookie), cookie);
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
    public static class WsFedAuthenticationEventExecutionPlanPrincipalConfiguration {
        @ConditionalOnMissingBean(name = "wsfedPrincipalFactory")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public PrincipalFactory wsfedPrincipalFactory() {
            return PrincipalFactoryUtils.newPrincipalFactory();
        }


    }

    @Configuration(value = "WsFedAuthenticationEventExecutionPlanBaseConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class WsFedAuthenticationEventExecutionPlanBaseConfiguration {
        @ConditionalOnMissingBean(name = "wsfedAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer wsfedAuthenticationEventExecutionPlanConfigurer(
            final CasConfigurationProperties casProperties,
            @Qualifier("wsfedPrincipalFactory")
            final PrincipalFactory wsfedPrincipalFactory,
            @Qualifier("wsFederationConfigurations")
            final BeanContainer<WsFederationConfiguration> wsFederationConfigurations,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY)
            final IPersonAttributeDao attributeRepository,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            val personDirectory = casProperties.getPersonDirectory();
            return plan -> casProperties.getAuthn()
                .getWsfed()
                .stream()
                .filter(wsfed -> StringUtils.isNotBlank(wsfed.getIdentityProviderUrl())
                                 && StringUtils.isNotBlank(wsfed.getIdentityProviderIdentifier()))
                .forEach(wsfed -> {
                    val handler = new WsFederationAuthenticationHandler(wsfed.getName(), servicesManager, wsfedPrincipalFactory, wsfed.getOrder());
                    if (!wsfed.isAttributeResolverEnabled()) {
                        plan.registerAuthenticationHandler(handler);
                    } else {
                        val cfg = wsFederationConfigurations.toSet().stream()
                            .filter(c -> {
                                val resolver = SpringExpressionLanguageValueResolver.getInstance();
                                return c.getIdentityProviderUrl().equalsIgnoreCase(resolver.resolve(wsfed.getIdentityProviderUrl()));
                            })
                            .findFirst()
                            .orElseThrow(() ->
                                new RuntimeException("Unable to find configuration for identity provider " + wsfed.getIdentityProviderUrl()));
                        val principal = wsfed.getPrincipal();
                        val resolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(wsfedPrincipalFactory, attributeRepository,
                            CoreAuthenticationUtils.getAttributeMerger(casProperties.getAuthn().getAttributeRepository().getCore().getMerger()),
                            WsFederationCredentialsToPrincipalResolver.class,
                            principal, personDirectory);
                        resolver.setConfiguration(cfg);
                        plan.registerAuthenticationHandlerWithPrincipalResolver(handler, resolver);
                    }
                });
        }
    }

}
