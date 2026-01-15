package org.apereo.cas.web.flow.resolver.impl;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationContextValidator;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.flow.SingleSignOnBuildingStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.support.ArgumentExtractor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CasWebflowEventResolutionConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class CasWebflowEventResolutionConfigurationContext {
    /**
     * The bean name of this component in the Spring context.
     */
    public static final String BEAN_NAME = "casWebflowConfigurationContext";
    
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final CentralAuthenticationService centralAuthenticationService;

    private final ServicesManager servicesManager;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final TicketRegistry ticketRegistry;

    private final CasCookieBuilder warnCookieGenerator;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final ConfigurableApplicationContext applicationContext;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final CasConfigurationProperties casProperties;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final List<ArgumentExtractor> argumentExtractors;

    private final PrincipalFactory principalFactory;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final MultifactorAuthenticationContextValidator authenticationContextValidator;

    private final CasWebflowCredentialProvider casWebflowCredentialProvider;

    private final SingleSignOnBuildingStrategy singleSignOnBuildingStrategy;

    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    private final CasWebflowExceptionCatalog casWebflowExceptionCatalog;

    private final TenantExtractor tenantExtractor;
}
