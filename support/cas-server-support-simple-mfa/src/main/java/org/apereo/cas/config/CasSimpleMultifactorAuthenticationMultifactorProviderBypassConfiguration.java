package org.apereo.cas.config;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypass;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casSimpleMultifactorBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(casSimpleMultifactorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorRestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(simple.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getSimple();
        val props = simple.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

}
