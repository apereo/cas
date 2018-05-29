package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * This is {@link CasCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class CasCoreConfiguration {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("logoutManager")
    private ObjectProvider<LogoutManager> logoutManager;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private ObjectProvider<TicketFactory> ticketFactory;

    @Autowired
    @Qualifier("principalFactory")
    private ObjectProvider<PrincipalFactory> principalFactory;

    @Autowired
    @Qualifier("protocolTicketCipherExecutor")
    private ObjectProvider<CipherExecutor> cipherExecutor;

    @Bean
    @ConditionalOnMissingBean(name = "authenticationPolicyFactory")
    public ContextualAuthenticationPolicyFactory<ServiceContext> authenticationPolicyFactory() {
        if (casProperties.getAuthn().getPolicy().isRequiredHandlerAuthenticationPolicyEnabled()) {
            return new RequiredHandlerAuthenticationPolicyFactory();
        }
        return new AcceptAnyAuthenticationPolicyFactory();
    }

    @ConditionalOnMissingBean(name = "authenticationServiceSelectionPlan")
    @Autowired
    @Bean
    public AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan(final List<AuthenticationServiceSelectionStrategyConfigurer> configurers) {
        final var plan = new DefaultAuthenticationServiceSelectionPlan();
        configurers.forEach(c -> {
            final var name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring authentication request service selection strategy plan [{}]", name);
            c.configureAuthenticationServiceSelectionStrategy(plan);
        });
        return plan;
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "centralAuthenticationService")
    public CentralAuthenticationService centralAuthenticationService(
        @Qualifier("authenticationServiceSelectionPlan") final AuthenticationServiceSelectionPlan authenticationServiceSelectionPlan) {
        return new DefaultCentralAuthenticationService(applicationEventPublisher,
            ticketRegistry.getIfAvailable(),
            servicesManager.getIfAvailable(),
            logoutManager.getIfAvailable(),
            ticketFactory.getIfAvailable(),
            authenticationServiceSelectionPlan,
            authenticationPolicyFactory(),
            principalFactory.getIfAvailable(),
            cipherExecutor.getIfAvailable(),
            registeredServiceAccessStrategyEnforcer);
    }
}
