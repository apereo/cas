package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypass;
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
    public MultifactorAuthenticationProviderBypass googleAuthenticatorBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getGauth().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(googleAuthenticatorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass googleAuthenticatorRestMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass googleAuthenticatorGroovyMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

    @ConditionalOnMissingBean(name = "googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass googleAuthenticatorHttpRequestMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass googleAuthenticatorCredentialMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass googleAuthenticatorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass googleAuthenticatorPrincipalMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass googleAuthenticatorAuthenticationMultifactorAuthenticationProviderBypass() {
        val gauth = casProperties.getAuthn().getMfa().getGauth();
        val props = gauth.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, gauth.getId());
    }

}
