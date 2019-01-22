package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
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
        val bypass = new ChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addBypass(yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addBypass(yubikeyAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addBypass(yubikeyCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addBypass(yubikeyHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addBypass(yubikeyGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addBypass(yubikeyRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "yubikeyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyRestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "yubikeyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyGroovyMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "yubikeyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass yubikeyHttpRequestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyCredentialMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyRegisteredServiceMultifactorAuthenticationProviderBypass() {
        return new RegisteredServiceMultifactorAuthenticationProviderBypass();
    }

    @Bean
    @ConditionalOnMissingBean(name = "yubikeyPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyPrincipalMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "yubikeyAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass yubikeyAuthenticationMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getYubikey().getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props);
    }

}
