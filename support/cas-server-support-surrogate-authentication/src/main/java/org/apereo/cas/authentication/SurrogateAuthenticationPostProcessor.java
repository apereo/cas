package org.apereo.cas.authentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
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
@AllArgsConstructor
public class SurrogateAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final PrincipalFactory principalFactory;
    private final SurrogateAuthenticationService surrogateAuthenticationService;
    private final ServicesManager servicesManager;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {
        final Authentication authentication = builder.build();
        final Principal principal = authentication.getPrincipal();
        final SurrogateUsernamePasswordCredential surrogateCredentials = (SurrogateUsernamePasswordCredential) transaction.getCredential();
        final String targetUserId = surrogateCredentials.getSurrogateUsername();

        try {

            if (StringUtils.isBlank(targetUserId)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }

            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next...", principal);
            if (transaction.getService() != null) {
                final RegisteredService svc = this.servicesManager.findServiceBy(transaction.getService());
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(transaction.getService(), svc, authentication);
            }

            if (this.surrogateAuthenticationService.canAuthenticateAs(targetUserId, principal, transaction.getService())) {
                LOGGER.debug("Principal [{}] is authorized to authenticate as [{}]", principal, targetUserId);
                builder.setPrincipal(this.principalFactory.createPrincipal(targetUserId));
                publishSuccessEvent(principal, targetUserId);
                return;
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", principal, targetUserId);
            publishFailureEvent(principal, targetUserId);
            throw new FailedLoginException();
        } catch (final Exception e) {
            publishFailureEvent(principal, targetUserId);
            final Map<String, Throwable> map = CollectionUtils.wrap(getClass().getSimpleName(),
                new SurrogateAuthenticationException("Principal " + principal+ " is unauthorized to authenticate as " + targetUserId));
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
