package org.apereo.cas.authentication;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SurrogateAuthenticationPostProcessor implements AuthenticationPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationPostProcessor.class);

    private final PrincipalFactory principalFactory;
    private final SurrogateAuthenticationService surrogateAuthenticationService;
    private final ServicesManager servicesManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SurrogateAuthenticationPostProcessor(final PrincipalFactory principalFactory,
                                                final SurrogateAuthenticationService surrogateAuthenticationService,
                                                final ServicesManager servicesManager,
                                                final ApplicationEventPublisher applicationEventPublisher) {
        this.principalFactory = principalFactory;
        this.surrogateAuthenticationService = surrogateAuthenticationService;
        this.servicesManager = servicesManager;
        this.applicationEventPublisher = applicationEventPublisher;
    }

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
            final Map<String, Class<? extends Throwable>> map = CollectionUtils.wrap(getClass().getSimpleName(), SurrogateAuthenticationException.class);
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
