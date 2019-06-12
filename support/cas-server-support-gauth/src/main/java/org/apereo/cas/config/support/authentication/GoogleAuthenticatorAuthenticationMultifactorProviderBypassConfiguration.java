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
 * This is {@link GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("googleAuthenticatorMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class GoogleAuthenticatorAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "googleAuthenticatorBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getGauth().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass());
        }

        bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass());

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(googleAuthenticatorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRestMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, gauth.getId());
    }

}
