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

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link SwivelAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "swivelAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class SwivelAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "swivelBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("swivelPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("swivelAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("swivelRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator swivelRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "swivelCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "swivelRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(swivel.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "swivelPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "swivelAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator swivelAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }
}
