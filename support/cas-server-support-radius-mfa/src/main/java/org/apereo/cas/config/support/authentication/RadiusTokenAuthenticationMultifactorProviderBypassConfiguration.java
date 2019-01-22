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
        val bypass = new ChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addBypass(radiusRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addBypass(radiusAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addBypass(radiusCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addBypass(radiusHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addBypass(radiusGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addBypass(radiusRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "radiusRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusRestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "radiusGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusGroovyMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "radiusHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass radiusHttpRequestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusCredentialMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusRegisteredServiceMultifactorAuthenticationProviderBypass() {
        return new RegisteredServiceMultifactorAuthenticationProviderBypass();
    }

    @Bean
    @ConditionalOnMissingBean(name = "radiusPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusPrincipalMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "radiusAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass radiusAuthenticationMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getRadius().getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props);
    }

}
