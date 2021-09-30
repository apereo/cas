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
 * This is {@link CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "casSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @ConditionalOnMissingBean(name = "casSimpleMultifactorBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorBypassEvaluator(
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
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass);
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass);
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(
            casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator);
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
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass);
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casSimpleMultifactorRestMultifactorAuthenticationProviderBypass);
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRestMultifactorAuthenticationProviderBypass(
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass(
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass(
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(
        final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(simple.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(simple.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    @Autowired
    public MultifactorAuthenticationProviderBypassEvaluator casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass(final CasConfigurationProperties casProperties) {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

}
