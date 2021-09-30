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
 * This is {@link U2FAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "u2fAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class U2FAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "u2fBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("u2fPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator u2fRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("u2fAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("u2fRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator u2fRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getU2f().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()!=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "u2fRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(u2f.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "u2fCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "u2fRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(u2f.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "u2fAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator u2fAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }
}
