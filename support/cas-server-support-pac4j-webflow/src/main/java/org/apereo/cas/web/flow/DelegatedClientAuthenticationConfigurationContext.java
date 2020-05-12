package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientWebflowManager;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.ArgumentExtractor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This is {@link DelegatedClientAuthenticationConfigurationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@Setter
@Builder
public class DelegatedClientAuthenticationConfigurationContext {
    /**
     * The Clients.
     */
    private final Clients clients;

    /**
     * The Services manager.
     */
    private final ServicesManager servicesManager;

    /**
     * The Delegated authentication policy enforcer.
     */
    private final AuditableExecution delegatedAuthenticationPolicyEnforcer;

    /**
     * The Delegated client webflow manager.
     */
    private final DelegatedClientWebflowManager delegatedClientWebflowManager;

    /**
     * The Authentication system support.
     */
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CentralAuthenticationService centralAuthenticationService;

    private final SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    private final SessionStore<JEEContext> sessionStore;

    private final DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper;

    private final CasConfigurationProperties casProperties;

    private final List<ArgumentExtractor> argumentExtractors;

    private final Function<RequestContext, Set<DelegatedClientIdentityProviderConfiguration>> delegatedClientIdentityProvidersFunction;

    private final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    private final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    private final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    private final CasCookieBuilder cookieGenerator;
}
