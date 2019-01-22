package org.apereo.cas.config.support.authentication;

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
    public MultifactorAuthenticationProviderBypass u2fBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getU2f().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(u2fRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "u2fRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass u2fRestMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass u2fGroovyMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

    @ConditionalOnMissingBean(name = "u2fHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass u2fHttpRequestMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass u2fCredentialMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass u2fRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(u2f.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "u2fPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass u2fPrincipalMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "u2fAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass u2fAuthenticationMultifactorAuthenticationProviderBypass() {
        val u2f = casProperties.getAuthn().getMfa().getU2f();
        val props = u2f.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, u2f.getId());
    }

}
