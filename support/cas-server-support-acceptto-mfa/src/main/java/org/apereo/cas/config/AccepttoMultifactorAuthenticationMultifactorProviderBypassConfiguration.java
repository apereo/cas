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
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getAcceptto().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorPrincipalMultifactorAuthenticationProviderBypass());
        }
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator());
        
        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
            || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
            || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }


    @ConditionalOnMissingBean(name = "casAccepttoMultifactorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(simple.getId());
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorRestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorGroovyMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @ConditionalOnMissingBean(name = "casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorHttpRequestMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorCredentialMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(simple.getId());
    }

    @Bean
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorPrincipalMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator casAccepttoMultifactorAuthenticationMultifactorAuthenticationProviderBypass() {
        val simple = casProperties.getAuthn().getMfa().getAcceptto();
        val props = simple.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, simple.getId());
    }

}
