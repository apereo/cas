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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link WebAuthnMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("webAuthnMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebAuthnMultifactorProviderBypassConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "webAuthnBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getWebAuthn().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator());
        
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(webAuthnRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId());
    }
    
    @ConditionalOnMissingBean(name = "webAuthnRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRestMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @ConditionalOnMissingBean(name = "webAuthnGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnGroovyMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @ConditionalOnMissingBean(name = "webAuthnHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnHttpRequestMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getU2f();
        val props = webAuthn.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnCredentialMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(webAuthn.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnPrincipalMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator webAuthnAuthenticationMultifactorAuthenticationProviderBypass() {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val props = webAuthn.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, webAuthn.getId());
    }

}
