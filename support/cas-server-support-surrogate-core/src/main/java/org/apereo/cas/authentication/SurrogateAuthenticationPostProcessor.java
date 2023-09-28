package org.apereo.cas.authentication;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationFailureEvent;
import org.apereo.cas.support.events.authentication.surrogate.CasSurrogateAuthenticationSuccessfulEvent;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ApplicationEventPublisher;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.util.Map;
import java.util.Optional;


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

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws Throwable {
        val authentication = builder.build();
        val principal = authentication.getPrincipal();

        if (!(principal instanceof final SurrogatePrincipal primaryPrincipal)) {
            LOGGER.trace("Provided principal is one intended for surrogate authentication");
            return;
        }
        val primaryCredential = transaction.getPrimaryCredential();
        if (primaryCredential.isEmpty()) {
            throw new AuthenticationException("Unable to determine primary credentials");
        }
        val surrogateUsername = primaryCredential.get().getCredentialMetadata()
            .getTrait(SurrogateCredentialTrait.class)
            .map(SurrogateCredentialTrait::getSurrogateUsername)
            .orElseThrow(() -> new AuthenticationException("Unable to determine surrogate credential"));

        try {
            if (StringUtils.isBlank(surrogateUsername)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }
            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next for [{}]...", primaryPrincipal, surrogateUsername);
            if (transaction.getService() != null) {
                val svc = servicesManager.findServiceBy(transaction.getService());

                val serviceAccessAudit = AuditableContext.builder()
                    .service(transaction.getService())
                    .authentication(authentication)
                    .registeredService(svc)
                    .build();

                val accessResult = registeredServiceAccessStrategyEnforcer.execute(serviceAccessAudit);
                accessResult.throwExceptionIfNeeded();
            }

            if (surrogateAuthenticationService.canImpersonate(surrogateUsername, primaryPrincipal.getPrimary(), Optional.ofNullable(transaction.getService()))) {
                LOGGER.debug("Principal [{}] is authorized to authenticate as [{}]", primaryPrincipal, surrogateUsername);
                publishSuccessEvent(primaryPrincipal, surrogateUsername);

                val surrogateEligibleAudit = AuditableContext.builder()
                    .service(transaction.getService())
                    .authentication(authentication)
                    .properties(CollectionUtils.wrap("targetUserId", surrogateUsername, "eligible", Boolean.TRUE))
                    .build();

                surrogateEligibilityAuditableExecution.execute(surrogateEligibleAudit);
                return;
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", primaryPrincipal, surrogateUsername);
            throw new FailedLoginException();
        } catch (final Exception e) {
            publishFailureEvent(primaryPrincipal, surrogateUsername);
            final Map<String, Throwable> map = CollectionUtils.wrap(getClass().getSimpleName(),
                new SurrogateAuthenticationException("Principal " + primaryPrincipal + " is unauthorized to authenticate as " + surrogateUsername));

            val surrogateIneligibleAudit = AuditableContext.builder()
                .service(transaction.getService())
                .authentication(authentication)
                .build();

            surrogateEligibilityAuditableExecution.execute(surrogateIneligibleAudit);
            throw new AuthenticationException(map);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
            .stream()
            .anyMatch(trait -> StringUtils.isNotBlank(trait.getSurrogateUsername()));
    }

    private void publishFailureEvent(final Principal principal, final String surrogate) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val event = new CasSurrogateAuthenticationFailureEvent(this, principal, surrogate, clientInfo);
        publishEvent(event);
    }

    private void publishSuccessEvent(final Principal principal, final String surrogate) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val event = new CasSurrogateAuthenticationSuccessfulEvent(this, principal, surrogate, clientInfo);
        publishEvent(event);
    }

    private void publishEvent(final AbstractCasEvent event) {
        if (this.applicationEventPublisher != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }
}
