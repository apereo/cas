package org.apereo.cas.web.flow.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.cipher.WebflowConversationStateCipherExecutor;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.AuthenticationExceptionHandlerAction;
import org.apereo.cas.web.flow.actions.CheckWebAuthenticationRequestAction;
import org.apereo.cas.web.flow.actions.ClearWebflowCredentialAction;
import org.apereo.cas.web.flow.actions.InjectResponseHeadersAction;
import org.apereo.cas.web.flow.actions.RedirectToServiceAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Action;

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
    private ObjectProvider<CookieGenerator> warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationServiceSelectionPlan;

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    @ConditionalOnMissingBean(name = "serviceTicketRequestWebflowEventResolver")
    @Bean
    @RefreshScope
    public CasWebflowEventResolver serviceTicketRequestWebflowEventResolver() {
        return new ServiceTicketRequestWebflowEventResolver(authenticationSystemSupport.getIfAvailable(),
            centralAuthenticationService.getIfAvailable(),
            servicesManager.getIfAvailable(),
            ticketRegistrySupport.getIfAvailable(),
            warnCookieGenerator.getIfAvailable(),
            authenticationServiceSelectionPlan.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer.getIfAvailable(),
            casProperties);
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
    @ConditionalOnMissingBean(name = "redirectToServiceAction")
    @RefreshScope
    public Action redirectToServiceAction() {
        return new RedirectToServiceAction(responseBuilderLocator.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "injectResponseHeadersAction")
    @RefreshScope
    public Action injectResponseHeadersAction() {
        return new InjectResponseHeadersAction(responseBuilderLocator.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean(name = "singleSignOnParticipationStrategy")
    @RefreshScope
    public SingleSignOnParticipationStrategy singleSignOnParticipationStrategy() {
        val sso = casProperties.getSso();
        return new DefaultSingleSignOnParticipationStrategy(servicesManager.getIfAvailable(),
            sso.isCreateSsoCookieOnRenewAuthn(),
            sso.isRenewAuthnEnabled());
    }

    @ConditionalOnMissingBean(name = "authenticationExceptionHandler")
    @Bean
    public Action authenticationExceptionHandler() {
        return new AuthenticationExceptionHandlerAction(handledAuthenticationExceptions());
    }

    @RefreshScope
    @Bean
    public Set<Class<? extends Throwable>> handledAuthenticationExceptions() {
        /*
         * Order is important here; We want the account policy exceptions to be handled
         * first before moving onto more generic errors. In the event that multiple handlers
         * are defined, where one fails due to account policy restriction and one fails
         * due to a bad password, we want the error associated with the account policy
         * to be processed first, rather than presenting a more generic error associated
         */
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(javax.security.auth.login.AccountLockedException.class);
        errors.add(javax.security.auth.login.CredentialExpiredException.class);
        errors.add(javax.security.auth.login.AccountExpiredException.class);
        errors.add(AccountDisabledException.class);
        errors.add(InvalidLoginLocationException.class);
        errors.add(AccountPasswordMustChangeException.class);
        errors.add(InvalidLoginTimeException.class);

        errors.add(javax.security.auth.login.AccountNotFoundException.class);
        errors.add(javax.security.auth.login.FailedLoginException.class);
        errors.add(UnauthorizedServiceForPrincipalException.class);
        errors.add(PrincipalException.class);
        errors.add(UnsatisfiedAuthenticationPolicyException.class);
        errors.add(UnauthorizedAuthenticationException.class);

        errors.addAll(casProperties.getAuthn().getExceptions().getExceptions());

        return errors;
    }
}
