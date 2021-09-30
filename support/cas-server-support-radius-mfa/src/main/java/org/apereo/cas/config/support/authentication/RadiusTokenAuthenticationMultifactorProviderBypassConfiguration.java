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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link RadiusTokenAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(name = "cas.authn.mfa.radius.client.inet-address")
@Configuration(value = "radiusMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class RadiusTokenAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "radiusBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("radiusPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("radiusAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("radiusRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator radiusRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()!=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "radiusRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(radius.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "radiusPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "radiusAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator radiusAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }
}
