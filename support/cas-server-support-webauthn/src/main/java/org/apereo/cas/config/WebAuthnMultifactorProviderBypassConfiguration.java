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
 * This is {@link WebAuthnMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnWebAuthnEnabled
@Configuration(value = "webAuthnMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class WebAuthnMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "webAuthnBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("webAuthnPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("webAuthnAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("webAuthnRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator webAuthnRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()!=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId());
    }

    @ConditionalOnMissingBean(name = "webAuthnRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @ConditionalOnMissingBean(name = "webAuthnGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @ConditionalOnMissingBean(name = "webAuthnHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getU2f();
        val props = webAuthn.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "webAuthnAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }
}
