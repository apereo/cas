package org.apereo.cas.config;

import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
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

/**
 * This is {@link CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SimpleMFA)
@Configuration(value = "CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
class CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "casSimpleMultifactorBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRestMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        final CasConfigurationProperties casProperties) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass);
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorRestMultifactorAuthenticationProviderBypass);
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {

        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.simple.bypass.rest.url").given(applicationContext.getEnvironment()))
            .supply(() -> {
                val simple = casProperties.getAuthn().getMfa().getSimple();
                val props = simple.getBypass();
                return new RestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(MultifactorAuthenticationProviderBypassEvaluator.class)
            .when(BeanCondition.on("cas.authn.mfa.simple.bypass.groovy.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> {
                val simple = casProperties.getAuthn().getMfa().getSimple();
                val props = simple.getBypass();
                return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
    }

    @ConditionalOnMissingBean(name = "casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(simple.getId(), applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(simple.getId(), applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId(), applicationContext);
    }

}
