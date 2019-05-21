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
 * This is {@link YubiKeyAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("yubikeyAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class YubiKeyAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "yubikeyBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypass(yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(yubikeyRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "yubikeyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyRestMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

    @ConditionalOnMissingBean(name = "yubikeyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyGroovyMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

    @ConditionalOnMissingBean(name = "yubikeyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyHttpRequestMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyCredentialMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(yubikey.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyPrincipalMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyAuthenticationMultifactorAuthenticationProviderBypass() {
        val yubikey = casProperties.getAuthn().getMfa().getYubikey();
        val props = yubikey.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, yubikey.getId());
    }

}
