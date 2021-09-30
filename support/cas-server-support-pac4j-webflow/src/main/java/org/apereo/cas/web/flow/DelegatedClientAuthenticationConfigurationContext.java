package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DelegatedClientAuthenticationConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@Setter
@SuperBuilder
public class DelegatedClientAuthenticationConfigurationContext {
    /**
     * Default implementation bean id.
     */
    public static final String DEFAULT_BEAN_NAME = "delegatedClientAuthenticationConfigurationContext";

    private final Clients clients;

    private final ServicesManager servicesManager;

    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CentralAuthenticationService centralAuthenticationService;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final SessionStore sessionStore;

    private final DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper;

    private final CasConfigurationProperties casProperties;

    private final ArgumentExtractor argumentExtractor;

    private final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProvidersProducer;

    private final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    private final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator;

    private final CasCookieBuilder delegatedClientCookieGenerator;

    private final TicketFactory ticketFactory;

    @Builder.Default
    private List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers = new ArrayList<>();
}
