package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;

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

    public SurrogateAuthenticationPostProcessor(final PrincipalFactory principalFactory,
                                                final SurrogateAuthenticationService surrogateAuthenticationService) {
        this.principalFactory = principalFactory;
        this.surrogateAuthenticationService = surrogateAuthenticationService;
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {
        try {
            final SurrogateUsernamePasswordCredential surrogateCredentials = (SurrogateUsernamePasswordCredential) transaction.getCredential();
            final String targetUserId = surrogateCredentials.getSurrogateUsername();
            if (StringUtils.isBlank(targetUserId)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }
            final Principal principal = builder.build().getPrincipal();
            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next...", principal);

            if (this.surrogateAuthenticationService.canAuthenticateAs(targetUserId, principal)) {
                LOGGER.debug("Principal [{}] is authorized to authenticate as [{}]", principal, targetUserId);
                builder.setPrincipal(this.principalFactory.createPrincipal(targetUserId));
                return;
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", principal, targetUserId);
            throw new FailedLoginException();
        } catch (final Throwable e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential.getClass().equals(SurrogateUsernamePasswordCredential.class);
    }
}
