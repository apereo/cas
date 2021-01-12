package org.apereo.cas.authentication.policy;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.authentication.exceptions.UniquePrincipalRequiredException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.HttpRequestUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;
import java.util.Optional;
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
public class UniquePrincipalAuthenticationPolicy extends BaseAuthenticationPolicy {
    private static final long serialVersionUID = 3974114391376732470L;

    private final TicketRegistry ticketRegistry;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication authentication,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Optional<Serializable> assertionResult) {
        val request = HttpRequestUtils.getHttpServletRequestFromRequestAttributes();
        val renew = request == null ? StringUtils.EMPTY : request.getParameter(CasProtocolConstants.PARAMETER_RENEW);

        if (assertionResult.isEmpty() && StringUtils.isBlank(renew)) {
            val authPrincipal = authentication.getPrincipal();
            val count = ticketRegistry.countSessionsFor(authPrincipal.getId());
            if (count > 0) {
                LOGGER.warn("[{}] cannot be satisfied for [{}]; [{}] sessions currently exist",
                    getName(), authPrincipal.getId(), count);
                throw new UniquePrincipalRequiredException();
            }
        }
        return AuthenticationPolicyExecutionResult.success();
    }
}
