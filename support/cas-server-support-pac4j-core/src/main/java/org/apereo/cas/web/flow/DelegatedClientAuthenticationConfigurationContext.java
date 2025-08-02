package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.slo.SingleLogoutRequestExecutor;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
import org.apereo.cas.pac4j.client.DelegatedClientNameExtractor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ConfigurableApplicationContext;

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
    public static final String BEAN_NAME = "delegatedClientAuthenticationConfigurationContext";

    private final DelegatedIdentityProviders identityProviders;

    private final List<DelegatedAuthenticationCredentialExtractor> credentialExtractors;

    private final ServicesManager servicesManager;

    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CentralAuthenticationService centralAuthenticationService;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final SessionStore sessionStore;

    private final DelegatedClientNameExtractor delegatedClientNameExtractor;

    private final CasConfigurationProperties casProperties;

    private final ArgumentExtractor argumentExtractor;

    private final DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProvidersProducer;

    private final DelegatedClientIdentityProviderConfigurationPostProcessor delegatedClientIdentityProviderConfigurationPostProcessor;

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Deprecated(since = "7.3.0", forRemoval = true)
    private final CasCookieBuilder delegatedClientDistributedSessionCookieGenerator;

    private final CasCookieBuilder delegatedClientCookieGenerator;

    private final TicketFactory ticketFactory;

    private final ConfigurableApplicationContext applicationContext;

    private final TicketRegistry ticketRegistry;

    @Builder.Default
    private List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers = new ArrayList<>();

    @Builder.Default
    private List<DelegatedClientIdentityProviderAuthorizer> delegatedClientIdentityProviderAuthorizers = new ArrayList<>();

    private final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy;

    private final SingleLogoutRequestExecutor singleLogoutRequestExecutor;

    private final LogoutExecutionPlan logoutExecutionPlan;
}
