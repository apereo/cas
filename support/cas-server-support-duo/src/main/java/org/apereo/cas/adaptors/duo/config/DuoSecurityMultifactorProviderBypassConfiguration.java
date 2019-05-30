package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.ChainingMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
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
 * This is {@link DuoSecurityMultifactorProviderBypassConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("duoSecurityMultifactorProviderBypassConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DuoSecurityMultifactorProviderBypassConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "duoSecurityBypassEvaluator")
    @Bean
    @RefreshScope
    public ChainingMultifactorAuthenticationProviderBypassEvaluator duoSecurityBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityPrincipalMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityAuthenticationMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityCredentialMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityHttpRequestMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityGroovyMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypassEvaluator(duoSecurityRestMultifactorAuthenticationProviderBypass());
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityRestMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getRest().getUrl()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new RestMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityGroovyMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> duo.getBypass().getGroovy().getLocation() != null)
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new GroovyMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityHttpRequestMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> {
                val props = duo.getBypass();
                return StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
            })
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityCredentialMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getCredentialClassType()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new CredentialMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps.forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(duo.getId())));
        return bypass;
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityPrincipalMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getPrincipalAttributeName()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new PrincipalMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypassEvaluator duoSecurityAuthenticationMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> {
                val props = duo.getBypass();
                return StringUtils.isNotBlank(props.getAuthenticationAttributeName())
                    || StringUtils.isNotBlank(props.getAuthenticationHandlerName())
                    || StringUtils.isNotBlank(props.getAuthenticationMethodName());
            })
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypassEvaluator(new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance();
        }
        return bypass;
    }

}
