package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.PrincipalMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.RestMultifactorAuthenticationProviderBypassEvaluator;
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
 * This is {@link U2FAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("u2fAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class U2FAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "u2fBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator u2fBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getU2f().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fRegisteredServiceMultifactorAuthenticationProviderBypass());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(u2fRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "u2fRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator u2fRestMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator u2fGroovyMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator u2fHttpRequestMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator u2fCredentialMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator u2fRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(u2f.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator u2fPrincipalMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator u2fAuthenticationMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, u2f.getId());
    }

}
