package org.apereo.cas.adaptors.authy.config.support.authentication;

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
 * This is {@link AuthyAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("authyAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AuthyAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "authyBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getAuthy().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(authyPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypass(authyRegisteredServiceMultifactorAuthenticationProviderBypass());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(authyAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(authyCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(authyHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(authyGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(authyRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "authyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyRestMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyGroovyMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass authyHttpRequestMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass authyCredentialMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass authyRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass authyPrincipalMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass authyAuthenticationMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, authy.getId());
    }

}
