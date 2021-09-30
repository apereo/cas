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
 * This is {@link YubiKeyAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "yubikeyAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class YubiKeyAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "yubikeyBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("yubikeyPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("yubikeyAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("yubikeyRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator yubikeyRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(yubikeyRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "yubikeyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }

    @ConditionalOnMissingBean(name = "yubikeyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }

    @ConditionalOnMissingBean(name = "yubikeyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }

    @ConditionalOnMissingBean(name = "yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(yubikey.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(yubikey.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator yubikeyAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, yubikey.getId());
    }
}
