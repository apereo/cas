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
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.impl.account.GroovyPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.JsonPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.RestfulPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.SimplePasswordlessUserAccountStore;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.impl.token.PasswordlessTokenCipherExecutor;
import org.apereo.cas.impl.token.RestfulPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link PasswordlessAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.PasswordlessAuthn)
@AutoConfiguration
public class PasswordlessAuthenticationConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.passwordless.core.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory passwordlessPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "passwordlessTokenAuthenticationHandler")
    public AuthenticationHandler passwordlessTokenAuthenticationHandler(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("passwordlessPrincipalFactory")
        final PrincipalFactory passwordlessPrincipalFactory,
        @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
        final PasswordlessTokenRepository passwordlessTokenRepository,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return BeanSupplier.of(AuthenticationHandler.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> new PasswordlessTokenAuthenticationHandler(null, servicesManager,
                        passwordlessPrincipalFactory, null, passwordlessTokenRepository))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = PasswordlessUserAccountStore.BEAN_NAME)
    public PasswordlessUserAccountStore passwordlessUserAccountStore(final CasConfigurationProperties casProperties) {
        val accounts = casProperties.getAuthn().getPasswordless().getAccounts();
        if (accounts.getJson().getLocation() != null) {
            return new JsonPasswordlessUserAccountStore(accounts.getJson().getLocation());
        }
        if (accounts.getGroovy().getLocation() != null) {
            return new GroovyPasswordlessUserAccountStore(accounts.getGroovy().getLocation());
        }
        if (StringUtils.isNotBlank(accounts.getRest().getUrl())) {
            return new RestfulPasswordlessUserAccountStore(accounts.getRest());
        }
        var simple = accounts.getSimple().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
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
        return new SimplePasswordlessUserAccountStore(simple);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "passwordlessCipherExecutor")
    public CipherExecutor passwordlessCipherExecutor(final CasConfigurationProperties casProperties) {
        val tokens = casProperties.getAuthn().getPasswordless().getTokens();
        val crypto = tokens.getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, PasswordlessTokenCipherExecutor.class);
        }
        return CipherExecutor.noOpOfSerializableToString();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = PasswordlessTokenRepository.BEAN_NAME)
    public PasswordlessTokenRepository passwordlessTokenRepository(final CasConfigurationProperties casProperties,
                                                                   @Qualifier("passwordlessCipherExecutor")
                                                                   final CipherExecutor passwordlessCipherExecutor) {
        val tokens = casProperties.getAuthn().getPasswordless().getTokens();
        val expiration = Beans.newDuration(tokens.getCore().getExpiration()).toSeconds();

        if (StringUtils.isNotBlank(tokens.getRest().getUrl())) {
            return new RestfulPasswordlessTokenRepository(expiration, tokens.getRest(), passwordlessCipherExecutor);
        }
        return new InMemoryPasswordlessTokenRepository(expiration, passwordlessCipherExecutor);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer passwordlessAuthenticationEventExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("passwordlessTokenAuthenticationHandler")
        final AuthenticationHandler passwordlessTokenAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(passwordlessTokenAuthenticationHandler, defaultPrincipalResolver))
            .otherwiseProxy()
            .get();
    }
}
