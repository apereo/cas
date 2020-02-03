package org.apereo.cas.config;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.PasswordlessTokenAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.account.GroovyPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.RestfulPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.SimplePasswordlessUserAccountStore;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.impl.token.PasswordlessTokenCipherExecutor;
import org.apereo.cas.impl.token.RestfulPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link PasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("passwordlessAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class PasswordlessAuthenticationConfiguration {

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Bean
    public PrincipalFactory passwordlessPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "passwordlessTokenAuthenticationHandler")
    public AuthenticationHandler passwordlessTokenAuthenticationHandler() {
        return new PasswordlessTokenAuthenticationHandler(null, servicesManager.getObject(),
            passwordlessPrincipalFactory(), null, passwordlessTokenRepository());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessUserAccountStore")
    public PasswordlessUserAccountStore passwordlessUserAccountStore() {
        val accounts = casProperties.getAuthn().getPasswordless().getAccounts();

        if (accounts.getGroovy().getLocation() != null) {
            return new GroovyPasswordlessUserAccountStore(accounts.getGroovy().getLocation());
        }

        if (StringUtils.isNotBlank(accounts.getRest().getUrl())) {
            return new RestfulPasswordlessUserAccountStore(accounts.getRest());
        }

        var simple = accounts.getSimple()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val account = new PasswordlessUserAccount();
                account.setUsername(entry.getKey());
                account.setName(entry.getKey());
                if (EmailValidator.getInstance().isValid(entry.getValue())) {
                    account.setEmail(entry.getValue());
                } else {
                    account.setPhone(entry.getValue());
                }
                return account;
            }));
        return new SimplePasswordlessUserAccountStore((Map) simple);
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessCipherExecutor")
    public CipherExecutor passwordlessCipherExecutor() {
        val tokens = casProperties.getAuthn().getPasswordless().getTokens();
        val crypto = tokens.getRest().getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, PasswordlessTokenCipherExecutor.class);
        }
        return CipherExecutor.noOpOfSerializableToString();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessTokenRepository")
    public PasswordlessTokenRepository passwordlessTokenRepository() {
        val tokens = casProperties.getAuthn().getPasswordless().getTokens();
        if (StringUtils.isNotBlank(tokens.getRest().getUrl())) {
            return new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor());
        }
        return new InMemoryPasswordlessTokenRepository(tokens.getExpireInSeconds());
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer passwordlessAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
            passwordlessTokenAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }

}
