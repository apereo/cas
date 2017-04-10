package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;

/**
 * This is {@link SurrogateAuthenticationAspect}.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Aspect
public class SurrogateAuthenticationAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateAuthenticationAspect.class);

    private final PrincipalFactory principalFactory;
    private final SurrogateAuthenticationService surrogateAuthenticationService;

    public SurrogateAuthenticationAspect(final PrincipalFactory principalFactory,
                                         final SurrogateAuthenticationService surrogateAuthenticationService) {
        this.principalFactory = principalFactory;
        this.surrogateAuthenticationService = surrogateAuthenticationService;
    }

    /**
     * Handle surrogate principal creation post authentication.
     *
     * @param jp         the jp
     * @param credential the credential
     * @return the object
     * @throws Throwable the throwable
     */
    @Around(value = "execution(public org.apereo.cas.authentication.HandlerResult "
            + "org.apereo.cas.authentication.AuthenticationHandler.authenticate(..)) "
            + "&& args(credential)")
    public Object handleSurrogate(final ProceedingJoinPoint jp, final Credential credential) throws Throwable {
        try {
            if (!credential.getClass().equals(SurrogateUsernamePasswordCredential.class)) {
                return jp.proceed();
            }
            final SurrogateUsernamePasswordCredential surrogateCredentials = (SurrogateUsernamePasswordCredential) credential;
            final String targetUserId = surrogateCredentials.getSurrogateUsername();
            if (StringUtils.isBlank(targetUserId)) {
                LOGGER.error("No surrogate username was specified as part of the credential");
                throw new CredentialNotFoundException("Missing surrogate username in credential");
            }
            final HandlerResult result = (HandlerResult) jp.proceed();
            LOGGER.debug("Authenticated [{}] will be checked for surrogate eligibility next...", result.getPrincipal());

            if (this.surrogateAuthenticationService.canAuthenticateAs(targetUserId, result.getPrincipal())) {
                final Principal principal = this.principalFactory.createPrincipal(targetUserId);
                final AuthenticationHandler handler = AuthenticationHandler.class.cast(jp.getTarget());
                return new DefaultHandlerResult(handler, new BasicCredentialMetaData(credential), principal);
            }
            LOGGER.error("Principal [{}] is unable/unauthorized to authenticate as [{}]", result.getPrincipal(), targetUserId);
            throw new FailedLoginException();
        } catch (final Throwable e) {
            throw e;
        }
    }
}
