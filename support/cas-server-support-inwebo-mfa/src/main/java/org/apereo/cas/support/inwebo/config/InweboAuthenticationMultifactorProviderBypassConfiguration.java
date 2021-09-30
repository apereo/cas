package org.apereo.cas.support.inwebo.config;

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
 * The Inwebo MFA provider bypass configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "inweboMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
public class InweboAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "inweboBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboBypassEvaluator(
        final CasConfigurationProperties casProperties,
        @Qualifier("inweboPrincipalMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboPrincipalMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboRegisteredServiceMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServiceMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator,
        @Qualifier("inweboAuthenticationMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboAuthenticationMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboCredentialMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboCredentialMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboHttpRequestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboHttpRequestMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboGroovyMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboGroovyMultifactorAuthenticationProviderBypass,
        @Qualifier("inweboRestMultifactorAuthenticationProviderBypass")
        final MultifactorAuthenticationProviderBypassEvaluator inweboRestMultifactorAuthenticationProviderBypass) {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getInwebo().getBypass();
        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName()) || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboAuthenticationMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboCredentialMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboHttpRequestMultifactorAuthenticationProviderBypass);
        }
        if (props.getGroovy().getLocation()!=null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboRestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboGroovyMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboHttpRequestMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "inweboAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator inweboAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }
}
