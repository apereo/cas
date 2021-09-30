package org.apereo.cas.adaptors.authy.config.support.authentication;

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
 * This is {@link AuthyAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "authyAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class AuthyAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "authyBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("authyPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("authyRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("authyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator authyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("authyAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("authyCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("authyHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("authyGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("authyRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator authyRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getAuthy().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders())
            || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()
            !=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "authyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(authy.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(authy.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator authyAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }
}
