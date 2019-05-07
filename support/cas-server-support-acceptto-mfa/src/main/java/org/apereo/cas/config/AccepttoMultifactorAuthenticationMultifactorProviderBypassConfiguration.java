package org.apereo.cas.config;

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
 * This is {@link AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getAcceptto().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypass(casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new RestMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        return new RegisteredServiceMultifactorAuthenticationProviderBypass(simple.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorPrincipalMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypass(props, simple.getId());
    }

}
