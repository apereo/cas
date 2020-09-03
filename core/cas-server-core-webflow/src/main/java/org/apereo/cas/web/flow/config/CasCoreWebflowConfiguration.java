package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.MessageBundleProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.ChainingSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategyConfigurer;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersAction;
import org.apereo.cas.web.flow.actions.RedirectToServiceAction;
import org.apereo.cas.web.flow.actions.RenewAuthenticationRequestCheckAction;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAbstractTicketExceptionHandler;
import org.apereo.cas.web.flow.authentication.DefaultCasWebflowAuthenticationExceptionHandler;
import org.apereo.cas.web.flow.authentication.GenericCasWebflowExceptionHandler;
import org.apereo.cas.web.flow.authentication.RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.flow.resolver.impl.ServiceTicketRequestWebflowEventResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Action;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link CasCoreWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasCoreWebflowConfiguration {

    @Autowired
    @Qualifier("centralAuthenticationService")
    private ObjectProvider<CentralAuthenticationService> centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private ObjectProvider<TicketRegistrySupport> ticketRegistrySupport;

    @Autowired
    @Qualifier("webApplicationResponseBuilderLocator")
    private ObjectProvider<ResponseBuilderLocator> responseBuilderLocator;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private ObjectProvider<CasCookieBuilder> warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("authenticationEventExecutionPlan")
    private ObjectProvider<AuthenticationEventExecutionPlan> authenticationEventExecutionPlan;

    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        val context = CasWebflowEventResolutionConfigurationContext.builder()
            .authenticationSystemSupport(authenticationSystemSupport.getObject())
            .centralAuthenticationService(centralAuthenticationService.getObject())
            .servicesManager(servicesManager.getObject())
            .ticketRegistrySupport(ticketRegistrySupport.getObject())
            .warnCookieGenerator(warnCookieGenerator.getObject())
            .authenticationRequestServiceSelectionStrategies(authenticationServiceSelectionPlan.getObject())
            .registeredServiceAccessStrategyEnforcer(registeredServiceAccessStrategyEnforcer.getObject())
            .casProperties(casProperties)
            .ticketRegistry(ticketRegistry.getObject())
            .applicationContext(applicationContext)
            .authenticationEventExecutionPlan(authenticationEventExecutionPlan.getObject())
            .build();
        return new ServiceTicketRequestWebflowEventResolver(context);
    }

    @Bean
    @RefreshScope
    public CipherExecutor webflowCipherExecutor() {
        val webflow = casProperties.getWebflow();
        val crypto = webflow.getCrypto();

        var enabled = crypto.isEnabled();
        if (!enabled && StringUtils.isNotBlank(crypto.getEncryption().getKey()) && StringUtils.isNotBlank(crypto.getSigning().getKey())) {
            LOGGER.warn("Webflow encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
                + "are defined for operations. CAS will proceed to enable the webflow encryption/signing functionality.");
            enabled = true;
        }
        if (enabled) {
            return new WebflowConversationStateCipherExecutor(
                crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg(),
                crypto.getSigning().getKeySize(),
                crypto.getEncryption().getKeySize());
        }
        LOGGER.warn("Webflow encryption/signing is turned off. This "
            + "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
            + "signing and verification of webflow state.");
        return CipherExecutor.noOp();
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS)
    @RefreshScope
    public Action clearWebflowCredentialsAction() {
        return new ClearWebflowCredentialAction();
    }

    @Bean
    @ConditionalOnMissingBean(name = "checkWebAuthenticationRequestAction")
    @RefreshScope
    public Action checkWebAuthenticationRequestAction() {
        return new CheckWebAuthenticationRequestAction(casProperties.getAuthn().getMfa().getContentType());
    }

    @Bean
    @ConditionalOnMissingBean(name = "renewAuthenticationRequestCheckAction")
    @RefreshScope
    public Action renewAuthenticationRequestCheckAction() {
        return new RenewAuthenticationRequestCheckAction(singleSignOnParticipationStrategy());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redirectToServiceAction")
    @RefreshScope
    public Action redirectToServiceAction() {
        return new RedirectToServiceAction(responseBuilderLocator.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "injectResponseHeadersAction")
    @RefreshScope
    public Action injectResponseHeadersAction() {
        return new InjectResponseHeadersAction(responseBuilderLocator.getObject());
    }

    @Bean
    @ConditionalOnMissingBean(name = "singleSignOnParticipationStrategy")
    @RefreshScope
    public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy() {
        val resolvers = applicationContext.getBeansOfType(SingleSignOnParticipationStrategyConfigurer.class, false, true);
        val providers = new ArrayList<SingleSignOnParticipationStrategyConfigurer>(resolvers.values());
        AnnotationAwareOrderComparator.sort(providers);

        val chain = new ChainingSingleSignOnParticipationStrategy();
        providers.forEach(provider -> provider.configureStrategy(chain));

        val sso = casProperties.getSso();
        val defaultStrategy = new DefaultSingleSignOnParticipationStrategy(servicesManager.getObject(),
            sso.isCreateSsoCookieOnRenewAuthn(),
            sso.isRenewAuthnEnabled(),
            ticketRegistrySupport.getObject(),
            authenticationServiceSelectionPlan.getObject());

        chain.addStrategy(defaultStrategy);
        return chain;
    }

    @ConditionalOnMissingBean(name = "defaultCasWebflowAuthenticationExceptionHandler")
    @Bean
    @RefreshScope
    public CasWebflowExceptionHandler defaultCasWebflowAuthenticationExceptionHandler() {
        return new DefaultCasWebflowAuthenticationExceptionHandler(
            handledAuthenticationExceptions(), MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @ConditionalOnMissingBean(name = "defaultCasWebflowAbstractTicketExceptionHandler")
    @Bean
    @RefreshScope
    public CasWebflowExceptionHandler defaultCasWebflowAbstractTicketExceptionHandler() {
        return new DefaultCasWebflowAbstractTicketExceptionHandler(
            handledAuthenticationExceptions(), MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }

    @ConditionalOnMissingBean(name = "genericCasWebflowExceptionHandler")
    @Bean
    @RefreshScope
    public CasWebflowExceptionHandler genericCasWebflowExceptionHandler() {
        return new GenericCasWebflowExceptionHandler(
            handledAuthenticationExceptions(), MessageBundleProperties.DEFAULT_BUNDLE_PREFIX_AUTHN_FAILURE);
    }


    @ConditionalOnMissingBean(name = "authenticationExceptionHandler")
    @Bean
    @RefreshScope
    public Action authenticationExceptionHandler() {
        val beans = applicationContext.getBeansOfType(CasWebflowExceptionHandler.class, false, true);
        val handlers = new ArrayList<>(beans.values());
        AnnotationAwareOrderComparator.sort(handlers);
        return new AuthenticationExceptionHandlerAction(handlers);
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "handledAuthenticationExceptions")
    public Set<Class<? extends Throwable>> handledAuthenticationExceptions() {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one fails due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(AccountLockedException.class);
        errors.add(CredentialExpiredException.class);
        errors.add(AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);
        errors.add(UniquePrincipalRequiredException.class);

        errors.add(AccountNotFoundException.class);
        errors.add(FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);
        errors.add(MultifactorAuthenticationProviderAbsentException.class);

        errors.addAll(casProperties.getAuthn().getErrors().getExceptions());
        return errors;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategy")
    public SingleSignOnParticipationStrategy requiredAuthenticationHandlersSingleSignOnParticipationStrategy() {
        return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager.getObject(),
            authenticationServiceSelectionPlan.getObject(), ticketRegistrySupport.getObject(),
            authenticationEventExecutionPlan.getObject(), applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(name = "requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer")
    @RefreshScope
    public SingleSignOnParticipationStrategyConfigurer requiredAuthenticationHandlersSingleSignOnParticipationStrategyConfigurer() {
        return chain -> chain.addStrategy(requiredAuthenticationHandlersSingleSignOnParticipationStrategy());
    }
}
