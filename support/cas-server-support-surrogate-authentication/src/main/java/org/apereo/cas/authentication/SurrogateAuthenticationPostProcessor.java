package org.apereo.cas.authentication;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Map;


/**
 * This is {@link SurrogateAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final SurrogateAuthenticationService surrogateAuthenticationService;
    private final ServicesManager servicesManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final AuditableExecution surrogateEligibilityAuditableExecution;
    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {
        final var authentication = builder.build();
        final var primaryPrincipal = authentication.getPrincipal();

        @NonNull
        final var surrogateCredentials = (SurrogateUsernamePasswordCredential) transaction.getPrimaryCredential().get();
        final var targetUserId = surrogateCredentials.getSurrogateUsername();

        try {
            if (StringUtils.isBlank(targetUserId)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }
            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next for [{}]...", primaryPrincipal, targetUserId);
            if (transaction.getService() != null) {
                final var svc = this.servicesManager.findServiceBy(transaction.getService());

                final var serviceAccessAudit = AuditableContext.builder()
                    .service(transaction.getService())
                    .authentication(authentication)
                    .registeredService(svc)
                    .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                    .build();

                final var accessResult = this.registeredServiceAccessStrategyEnforcer.execute(serviceAccessAudit);
                accessResult.throwExceptionIfNeeded();
            }

            if (this.surrogateAuthenticationService.canAuthenticateAs(targetUserId, primaryPrincipal, transaction.getService())) {
                LOGGER.debug("Principal [{}] is authorized to authenticate as [{}]", primaryPrincipal, targetUserId);
                publishSuccessEvent(primaryPrincipal, targetUserId);

                final var surrogateEligibleAudit = AuditableContext.builder()
                    .service(transaction.getService())
                    .authentication(authentication)
                    .properties(CollectionUtils.wrap("targetUserId", targetUserId, "eligible", Boolean.TRUE))
                    .build();

                this.surrogateEligibilityAuditableExecution.execute(surrogateEligibleAudit);
                return;
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", primaryPrincipal, targetUserId);
            throw new FailedLoginException();
        } catch (final Exception e) {
            publishFailureEvent(primaryPrincipal, targetUserId);
            final Map<String, Throwable> map = CollectionUtils.wrap(getClass().getSimpleName(),
                new SurrogateAuthenticationException("Principal " + primaryPrincipal + " is unauthorized to authenticate as " + targetUserId));

            final var surrogateIneligibleAudit = AuditableContext.builder()
                .service(transaction.getService())
                .authentication(authentication)
                .build();

            this.surrogateEligibilityAuditableExecution.execute(surrogateIneligibleAudit);
            throw new AuthenticationException(map);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getClass().equals(SurrogateUsernamePasswordCredential.class);
    }

    private void publishFailureEvent(final Principal principal, final String surrogate) {
        final AbstractCasEvent event = new CasSurrogateAuthenticationFailureEvent(this, principal, surrogate);
        publishEvent(event);
    }

    private void publishSuccessEvent(final Principal principal, final String surrogate) {
        final AbstractCasEvent event = new CasSurrogateAuthenticationSuccessfulEvent(this, principal, surrogate);
        publishEvent(event);
    }

    private void publishEvent(final AbstractCasEvent event) {
        if (this.applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }
}
