package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.configuration.model.core.authentication.policy.UniquePrincipalAuthenticationPolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link UniquePrincipalAuthenticationPolicy}
 * that prevents authentication if the same principal id
 * is found more than one in the registry. This effectively forces
 * each user to have a single and unique SSO session, disallowing
 * multiple logins.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@RequiredArgsConstructor
@Accessors(chain = true)
public class UniquePrincipalAuthenticationPolicy extends BaseAuthenticationPolicy {
    @Serial
    private static final long serialVersionUID = 3974114391376732470L;

    private final TicketRegistry ticketRegistry;

    private final ObjectProvider<SingleSignOnParticipationStrategy> singleSignOnParticipationStrategy;

    private final UniquePrincipalAuthenticationPolicyProperties properties;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication authentication,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Map<String, ? extends Serializable> context) throws Throwable {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .build();
        val strategy = singleSignOnParticipationStrategy.getObject();
        val ssoParticipation = strategy.supports(ssoRequest) && strategy.isParticipating(ssoRequest);
        if (!context.containsKey(Assertion.class.getName()) && ssoParticipation) {
            val authPrincipal = authentication.getPrincipal();
            val count = ticketRegistry.countSessionsFor(authPrincipal.getId());
            if (count > properties.getMaximumAllowedSessions()) {
                LOGGER.warn("[{}] cannot be satisfied for [{}]; [{}] sessions currently exist",
                    getName(), authPrincipal.getId(), count);
                throw new UniquePrincipalRequiredException();
            }
        }
        return AuthenticationPolicyExecutionResult.success();
    }
}
