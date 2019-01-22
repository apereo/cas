package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationBypassProvider;
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
    public MultifactorAuthenticationProviderBypass radiusBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(radiusRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "radiusRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusRestMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusGroovyMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

    @ConditionalOnMissingBean(name = "radiusHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusHttpRequestMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusCredentialMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(radius.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "radiusPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusPrincipalMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusAuthenticationMultifactorAuthenticationProviderBypass() {
        val radius = casProperties.getAuthn().getMfa().getRadius();
        val props = radius.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, radius.getId());
    }

}
