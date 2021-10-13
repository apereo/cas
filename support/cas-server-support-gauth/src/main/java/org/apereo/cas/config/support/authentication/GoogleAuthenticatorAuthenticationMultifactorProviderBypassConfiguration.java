package org.apereo.cas.config.support.authentication;

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
 * This is {@link GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "googleAuthenticatorMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "googleAuthenticatorBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier(
            "googleAuthenticatorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("googleAuthenticatorRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getGauth().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()!=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(gauth.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }
}
