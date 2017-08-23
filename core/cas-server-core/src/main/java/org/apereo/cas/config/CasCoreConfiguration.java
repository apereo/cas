package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.DefaultCentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.policy.AcceptAnyAuthenticationPolicyFactory;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategyConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.services.ServiceContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
public class CasCoreConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("logoutManager")
    private LogoutManager logoutManager;

    @Autowired
    @Qualifier("defaultTicketFactory")
    private TicketFactory ticketFactory;

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
        final DefaultAuthenticationServiceSelectionPlan plan = new DefaultAuthenticationServiceSelectionPlan();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring authentication request service selection strategy plan [{}]", name);
            c.configureAuthenticationServiceSelectionStrategy(plan);
        });
        return plan;
    }

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "centralAuthenticationService")
    public CentralAuthenticationService centralAuthenticationService(
            @Qualifier("authenticationServiceSelectionPlan")
            final AuthenticationServiceSelectionPlan selectionStrategies,
            @Qualifier("principalFactory")
            final PrincipalFactory principalFactory,
            @Qualifier("protocolTicketCipherExecutor")
            final CipherExecutor cipherExecutor) {
        return new DefaultCentralAuthenticationService(ticketRegistry, ticketFactory, 
                servicesManager, logoutManager,
                selectionStrategies, authenticationPolicyFactory(), 
                principalFactory, cipherExecutor);
    }
}
