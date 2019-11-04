package org.apereo.cas.config;

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
import org.apereo.cas.util.io.CommunicationsManager;
import org.apereo.cas.web.flow.AcceptPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.DisplayBeforePasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.PasswordlessAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.PrepareForPasswordlessAuthenticationAction;
import org.apereo.cas.web.flow.VerifyPasswordlessAccountAuthenticationAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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
public class PasswordlessAuthenticationConfiguration {
    @Autowired
    @Qualifier("communicationsManager")
    private ObjectProvider<CommunicationsManager> communicationsManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private ObjectProvider<FlowDefinitionRegistry> loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ObjectProvider<FlowBuilderServices> flowBuilderServices;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

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

    @Bean
    @ConditionalOnMissingBean(name = "verifyPasswordlessAccountAuthenticationAction")
    @RefreshScope
    public Action verifyPasswordlessAccountAuthenticationAction() {
        return new VerifyPasswordlessAccountAuthenticationAction(
            passwordlessTokenRepository(),
            passwordlessUserAccountStore());
    }

    @Bean
    @ConditionalOnMissingBean(name = "acceptPasswordlessAuthenticationAction")
    @RefreshScope
    public Action acceptPasswordlessAuthenticationAction() {
        return new AcceptPasswordlessAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            passwordlessTokenRepository(),
            authenticationSystemSupport.getObject(),
            passwordlessUserAccountStore());
    }

    @Bean
    @ConditionalOnMissingBean(name = "displayBeforePasswordlessAuthenticationAction")
    @RefreshScope
    public Action displayBeforePasswordlessAuthenticationAction() {
        return new DisplayBeforePasswordlessAuthenticationAction(passwordlessTokenRepository(),
            passwordlessUserAccountStore(), communicationsManager.getObject(), casProperties.getAuthn().getPasswordless());
    }

    @Bean
    public Action initializeLoginAction() {
        return new PrepareForPasswordlessAuthenticationAction(servicesManager.getObject());
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationWebflowConfigurer")
    @Bean
    @DependsOn("defaultWebflowConfigurer")
    public CasWebflowConfigurer passwordlessAuthenticationWebflowConfigurer() {
        return new PasswordlessAuthenticationWebflowConfigurer(flowBuilderServices.getObject(),
            loginFlowDefinitionRegistry.getObject(), applicationContext, casProperties);
    }

    @ConditionalOnMissingBean(name = "passwordlessAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer passwordlessAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(
            passwordlessTokenAuthenticationHandler(), defaultPrincipalResolver.getObject());
    }

    @ConditionalOnMissingBean(name = "passwordlessCasWebflowExecutionPlanConfigurer")
    @Bean
    public CasWebflowExecutionPlanConfigurer passwordlessCasWebflowExecutionPlanConfigurer() {
        return plan -> plan.registerWebflowConfigurer(passwordlessAuthenticationWebflowConfigurer());
    }
}
