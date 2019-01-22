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
    public MultifactorAuthenticationProviderBypass swivelBypassEvaluator() {
        val bypass = new ChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addBypass(swivelRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addBypass(swivelAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addBypass(swivelCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addBypass(swivelHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addBypass(swivelGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addBypass(swivelRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "swivelRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass swivelRestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "swivelGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass swivelGroovyMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props);
    }

    @ConditionalOnMissingBean(name = "swivelHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass swivelHttpRequestMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass swivelCredentialMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass swivelRegisteredServiceMultifactorAuthenticationProviderBypass() {
        return new RegisteredServiceMultifactorAuthenticationProviderBypass();
    }

    @Bean
    @ConditionalOnMissingBean(name = "swivelPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass swivelPrincipalMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "swivelAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass swivelAuthenticationMultifactorAuthenticationProviderBypass() {
        val props = casProperties.getAuthn().getMfa().getSwivel().getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props);
    }

}
