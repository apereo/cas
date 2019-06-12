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
 * This is {@link RadiusTokenAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("radiusMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RadiusTokenAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "radiusBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator radiusBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusRegisteredServiceMultifactorAuthenticationProviderBypass());
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(radiusRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "radiusRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator radiusRestMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator radiusGroovyMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator radiusHttpRequestMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusCredentialMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(radius.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "radiusPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusPrincipalMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator radiusAuthenticationMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, radius.getId());
    }

}
