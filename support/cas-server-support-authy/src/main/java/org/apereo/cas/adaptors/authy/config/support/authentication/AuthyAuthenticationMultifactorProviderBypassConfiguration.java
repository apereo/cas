package org.apereo.cas.adaptors.authy.config.support.authentication;

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
    public MultifactorAuthenticationProviderBypassEvaluator authyBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getAuthy().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyRegisteredServiceMultifactorAuthenticationProviderBypass());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(authyRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "authyRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator authyRestMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator authyGroovyMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @ConditionalOnMissingBean(name = "authyHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator authyHttpRequestMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator authyCredentialMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator authyRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator authyPrincipalMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "authyAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator authyAuthenticationMultifactorAuthenticationProviderBypass() {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val props = authy.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, authy.getId());
    }

}
