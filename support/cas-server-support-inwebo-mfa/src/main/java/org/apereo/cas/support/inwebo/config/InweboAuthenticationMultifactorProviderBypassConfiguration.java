package org.apereo.cas.support.inwebo.config;

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
 * The Inwebo MFA provider bypass configuration.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@Configuration("inweboMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InweboAuthenticationMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "inweboBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator inweboBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val props = casProperties.getAuthn().getMfa().getInwebo().getBypass();

        if (StringUtils.isNotBlank(props.getPrincipalAttributeName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboPrincipalMultifactorAuthenticationProviderBypass());
        }

        bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator());

        if (StringUtils.isNotBlank(props.getAuthenticationAttributeName())
                || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
                || StringUtils.isNotBlank(props.getAuthenticationMethodName())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboAuthenticationMultifactorAuthenticationProviderBypass());
        }

        if (StringUtils.isNotBlank(props.getCredentialClassType())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboCredentialMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboHttpRequestMultifactorAuthenticationProviderBypass());
        }
        if (props.getGroovy().getLocation() != null) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboGroovyMultifactorAuthenticationProviderBypass());
        }
        if (StringUtils.isNotBlank(props.getRest().getUrl())) {
            bypass.addMultifactorAuthenticationProviderBypassEvaluator(inweboRestMultifactorAuthenticationProviderBypass());
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServicePrincipalAttributeMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator inweboRestMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new RestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator inweboGroovyMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new GroovyMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @ConditionalOnMissingBean(name = "inweboHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator inweboHttpRequestMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "inweboCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboCredentialMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new CredentialMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "inweboRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        return new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(inwebo.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "inweboPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboPrincipalMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new PrincipalMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "inweboAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator inweboAuthenticationMultifactorAuthenticationProviderBypass() {
        val inwebo = casProperties.getAuthn().getMfa().getInwebo();
        val props = inwebo.getBypass();
        return new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(props, inwebo.getId());
    }
}
