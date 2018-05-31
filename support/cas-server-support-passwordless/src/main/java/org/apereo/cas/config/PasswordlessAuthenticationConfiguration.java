package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PasswordlessTokenAuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.account.GroovyPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.RestfulPasswordlessUserAccountStore;
import org.apereo.cas.impl.account.SimplePasswordlessUserAccountStore;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.impl.token.PasswordlessTokenCipherExecutor;
import org.apereo.cas.impl.token.RestfulPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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
@Slf4j
public class PasswordlessAuthenticationConfiguration implements CasWebflowExecutionPlanConfigurer {
    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Bean
    public PrincipalFactory passwordlessPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "passwordlessTokenAuthenticationHandler")
    public AuthenticationHandler passwordlessTokenAuthenticationHandler() {
        return new PasswordlessTokenAuthenticationHandler(null, servicesManager,
            passwordlessPrincipalFactory(), null, passwordlessTokenRepository());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessUserAccountStore")
    public PasswordlessUserAccountStore passwordlessUserAccountStore() {
        final var accounts = casProperties.getAuthn().getPasswordless().getAccounts();

        if (accounts.getGroovy().getLocation() != null) {
            return new GroovyPasswordlessUserAccountStore(accounts.getGroovy().getLocation());
        }

        if (StringUtils.isNotBlank(accounts.getRest().getUrl())) {
            return new RestfulPasswordlessUserAccountStore(accounts.getRest());
        }

        final Map simple = accounts.getSimple()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final var account = new PasswordlessUserAccount();
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
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessCipherExecutor")
    public CipherExecutor passwordlessCipherExecutor() {
        final var tokens = casProperties.getAuthn().getPasswordless().getTokens();
        final var crypto = tokens.getRest().getCrypto();
        final CipherExecutor cipher;
        if (crypto.isEnabled()) {
            cipher = new PasswordlessTokenCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg());
        } else {
            cipher = CipherExecutor.noOpOfSerializableToString();
        }
        return cipher;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "passwordlessTokenRepository")
    public PasswordlessTokenRepository passwordlessTokenRepository() {
        final var tokens = casProperties.getAuthn().getPasswordless().getTokens();
        if (StringUtils.isNotBlank(tokens.getRest().getUrl())) {
            return new RestfulPasswordlessTokenRepository(tokens.getExpireInSeconds(), tokens.getRest(), passwordlessCipherExecutor());
        }
        return new InMemoryPasswordlessTokenRepository(tokens.getExpireInSeconds());
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptPasswordlessAuthenticationAction")
    public Action acceptPasswordlessAuthenticationAction() {
        return new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver,
            adaptiveAuthenticationPolicy.getIfAvailable(),
            passwordlessTokenRepository(),
            authenticationSystemSupport.getIfAvailable(),
            passwordlessUserAccountStore());
    }

    @Bean
    @ConditionalOnMissingBean(name = "displayBeforePasswordlessAuthenticationAction")
    public Action displayBeforePasswordlessAuthenticationAction() {
        return new DisplayBeforePasswordlessAuthenticationAction(passwordlessTokenRepository(),
            passwordlessUserAccountStore(), communicationsManager, casProperties.getAuthn().getPasswordless());
    }

    @Bean
    public Action initializeLoginAction() {
        return new PrepareForPasswordlessAuthenticationAction(servicesManager);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer() {
        return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices,
            loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer passwordlessAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandler(passwordlessTokenAuthenticationHandler());
    }

    @Override
    public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(passwordlessAuthenticationWebflowConfigurer());
    }
}
