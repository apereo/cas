package org.apereo.cas.config;

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
 * This is {@link CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("casSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSimpleMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casSimpleMultifactorBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorBypassEvaluator() {
        val bypass = new ChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addBypass(casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addBypass(casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addBypass(casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addBypass(casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addBypass(casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addBypass(casSimpleMultifactorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorRestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorGroovyMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorHttpRequestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorCredentialMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        return new RegisteredServiceMultifactorAuthenticationProviderBypass();
    }

    @Bean
    @ConditionalOnMissingBean(name = "casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorPrincipalMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casSimpleMultifactorAuthenticationMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSimple().getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props);
    }

}
