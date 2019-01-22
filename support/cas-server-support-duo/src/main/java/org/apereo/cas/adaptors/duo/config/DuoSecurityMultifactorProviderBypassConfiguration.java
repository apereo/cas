package org.apereo.cas.adaptors.duo.config;

import org.apereo.cas.authentication.ChainingMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationBypassProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.AuthenticationMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.CredentialMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.GroovyMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypass;
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
    public ChainingMultifactorAuthenticationProviderBypass duoSecurityBypassEvaluator() {
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityAuthenticationMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityCredentialMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityHttpRequestMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityGroovyMultifactorAuthenticationProviderBypass());
        bypass.addMultifactorAuthenticationProviderBypass(duoSecurityRestMultifactorAuthenticationProviderBypass());
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityRestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass duoSecurityRestMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getRest().getUrl()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new RestMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityGroovyMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass duoSecurityGroovyMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> duo.getBypass().getGroovy().getLocation() != null)
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new GroovyMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

    @ConditionalOnMissingBean(name = "duoSecurityHttpRequestMultifactorAuthenticationProviderBypass")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass duoSecurityHttpRequestMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> {
                val props = duo.getBypass();
                return StringUtils.isNotBlank(props.getHttpRequestHeaders()) || StringUtils.isNotBlank(props.getHttpRequestRemoteAddress());
            })
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new HttpRequestMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityCredentialMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass duoSecurityCredentialMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getCredentialClassType()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new CredentialMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass duoSecurityRegisteredServiceMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps.forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new RegisteredServiceMultifactorAuthenticationProviderBypass(duo.getId())));
        return bypass;
    }

    @Bean
    @ConditionalOnMissingBean(name = "duoSecurityPrincipalMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass duoSecurityPrincipalMultifactorAuthenticationProviderBypass() {
        val duoProps = casProperties.getAuthn().getMfa().getDuo();
        val bypass = new DefaultChainingMultifactorAuthenticationBypassProvider();
        duoProps
            .stream()
            .filter(duo -> StringUtils.isNotBlank(duo.getBypass().getPrincipalAttributeName()))
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new PrincipalMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "duoSecurityAuthenticationMultifactorAuthenticationProviderBypass")
    public MultifactorAuthenticationProviderBypass duoSecurityAuthenticationMultifactorAuthenticationProviderBypass() {
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
            .forEach(duo -> bypass.addMultifactorAuthenticationProviderBypass(new AuthenticationMultifactorAuthenticationProviderBypass(duo.getBypass(), duo.getId())));
        if (bypass.isEmpty()) {
            return NeverAllowMultifactorAuthenticationProviderBypass.getInstance();
        }
        return bypass;
    }

}
