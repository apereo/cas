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
 * This is {@link SwivelAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("swivelAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SwivelAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "swivelBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(
            swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(swivelRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelRestMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelGroovyMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @ConditionalOnMissingBean(name = "swivelHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelHttpRequestMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator swivelCredentialMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator swivelRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(swivel.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "swivelPrincipalMultifactorAuthenticationProviderBypass")
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator swivelPrincipalMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator swivelAuthenticationMultifactorAuthenticationProviderBypass() {
        val swivel = casProperties.getAuthn().getMfa().getSwivel();
        val props = swivel.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, swivel.getId());
    }

}
