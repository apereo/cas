package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableEntity;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Inspektr {@link PrincipalResolver} that gets the value for principal id
 * from {@link Authentication} object bound to a current thread of execution.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAuditPrincipalResolver implements PrincipalResolver {
    private final AuditPrincipalIdProvider auditPrincipalIdProvider;
    private final CasWebflowCredentialProvider webflowCredentialProvider;
    
    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        LOGGER.trace("Resolving principal at audit point [{}]", auditTarget);
        return getCurrentPrincipal(auditTarget, returnValue, null);
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        LOGGER.trace("Resolving principal at audit point [{}] with exception [{}]", auditTarget, exception.getMessage());
        return getCurrentPrincipal(auditTarget, null, exception);
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    protected String getCurrentPrincipal(final JoinPoint auditTarget, final Object returnValue, final Exception exception) {
        var currentPrincipal = UNKNOWN_USER;
        if (auditTarget.getArgs() != null && auditTarget.getArgs().length > 0) {
            val firstArgument = auditTarget.getArgs()[0];
            currentPrincipal = switch (firstArgument) {
                case final RequestContext requestContext -> getPrincipalFromRequestContext(auditTarget, returnValue, exception, requestContext);
                case final AuthenticationTransaction authenticationTransaction -> getPrincipalFromAuthenticationTransaction(authenticationTransaction);
                case final SingleLogoutExecutionRequest sloRequest -> getPrincipalFromSingleLogoutRequest(auditTarget, returnValue, exception, sloRequest);
                case final Authentication authentication -> getPrincipalFromAuthentication(auditTarget, returnValue, exception, authentication);
                case final AuthenticationResult authenticationResult -> getPrincipalFromAuthenticationResult(auditTarget, returnValue, exception, authenticationResult);
                case final AuditableContext auditableContext -> getPrincipalFromAuditContext(auditTarget, returnValue, exception, auditableContext);
                case final AuditableEntity auditableEntity -> getPrincipalFromAuditableEntity(auditTarget, returnValue, exception, auditableEntity);
                case final Assertion assertion -> getPrincipalFromAssertion(auditTarget, returnValue, exception, assertion);
                case final Credential credential -> getPrincipalFromCredential(auditTarget, returnValue, exception, credential);
                case final HttpServletRequest httpServletRequest -> getPrincipalFromRequest(auditTarget, returnValue, exception, httpServletRequest);
                default -> UNKNOWN_USER;
            };
        }
        if (Strings.CI.equals(currentPrincipal, UNKNOWN_USER) && returnValue != null) {
            currentPrincipal = switch (returnValue) {
                case final AuthenticationAwareTicket ticket -> getPrincipalFromTicket(auditTarget, returnValue, exception, ticket);
                case final AuditableContext auditableContext -> getPrincipalFromAuditContext(auditTarget, returnValue, exception, auditableContext);
                case final Assertion assertion -> getPrincipalFromAssertion(auditTarget, returnValue, exception, assertion);
                case final AuditableEntity auditableEntity -> getPrincipalFromAuditableEntity(auditTarget, returnValue, exception, auditableEntity);
                default -> UNKNOWN_USER;
            };
        }
        return currentPrincipal;
    }

    protected String getPrincipalFromRequest(final JoinPoint auditTarget, final Object returnValue,
                                             final Exception exception, final HttpServletRequest httpServletRequest) {
        return Optional.ofNullable(httpServletRequest.getAttribute(Principal.class.getName()))
            .map(Principal.class::cast)
            .map(Principal::getId)
            .orElse(UNKNOWN_USER);
    }

    protected String getPrincipalFromCredential(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                                final Credential credential) {
        return StringUtils.defaultIfBlank(credential.getId(), UNKNOWN_USER);
    }

    protected String getPrincipalFromAssertion(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                               final Assertion assertion) {
        val authentication = assertion.getPrimaryAuthentication();
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
    }

    protected String getPrincipalFromAuditContext(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                                  final AuditableContext auditableContext) {
        return auditableContext.getAuthentication()
            .map(authentication -> {
                val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
                return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
            })
            .or(() -> auditableContext.getPrincipal().map(Principal::getId))
            .orElse(UNKNOWN_USER);
    }

    protected String getPrincipalFromTicket(final JoinPoint auditTarget, final Object returnValue, final Exception exception, final AuthenticationAwareTicket ticket) {
        val authentication = ticket.getAuthentication();
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
    }

    protected String getPrincipalFromAuthenticationResult(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                                          final AuthenticationResult authenticationResult) {
        val authentication = authenticationResult.getAuthentication();
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
    }

    protected String getPrincipalFromAuditableEntity(final JoinPoint auditTarget, final Object returnValue,
                                                     final Exception exception, final AuditableEntity entity) {
        return StringUtils.defaultIfBlank(entity.getAuditablePrincipal(), UNKNOWN_USER);
    }

    protected String getPrincipalFromAuthentication(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                                    final Authentication authentication) {
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
    }

    protected String getPrincipalFromSingleLogoutRequest(final JoinPoint auditTarget, final Object returnValue, final Exception exception,
                                                         final SingleLogoutExecutionRequest sloRequest) {
        val authentication = sloRequest.getTicketGrantingTicket().getAuthentication();
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        return StringUtils.defaultIfBlank(principalId, UNKNOWN_USER);
    }

    protected String getPrincipalFromAuthenticationTransaction(final AuthenticationTransaction authenticationTransaction) {
        val credentialId = authenticationTransaction.getPrimaryCredential().map(Credential::getId).orElse(UNKNOWN_USER);
        return StringUtils.defaultIfBlank(credentialId, UNKNOWN_USER);
    }

    protected String getPrincipalFromRequestContext(final JoinPoint auditTarget, final Object returnValue,
                                                    final Exception exception, final RequestContext requestContext) {
        val credentials = webflowCredentialProvider.extract(requestContext);
        val credentialId = credentials.stream().map(Credential::getId).findFirst().orElse(UNKNOWN_USER);

        val authentication = WebUtils.getAuthentication(requestContext);
        val principalId = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
        val id = Optional.ofNullable(principalId)
            .or(() -> Optional.ofNullable(authentication).map(Authentication::getPrincipal).map(Principal::getId))
            .orElse(credentialId);
        return StringUtils.defaultIfBlank(id, UNKNOWN_USER);
    }
}
