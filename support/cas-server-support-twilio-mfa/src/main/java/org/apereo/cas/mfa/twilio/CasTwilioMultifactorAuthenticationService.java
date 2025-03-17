package org.apereo.cas.mfa.twilio;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;

/**
 * This is {@link CasTwilioMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
public interface CasTwilioMultifactorAuthenticationService {
    /**
     * The bean name of the service.
     */
    String BEAN_NAME = "casTwilioMultifactorAuthenticationService";

    /**
     * Generate token.
     *
     * @param principal the principal
     * @param service   the service
     * @return true or false
     */
    boolean generateToken(Principal principal, WebApplicationService service);

    /**
     * Validate token and return principal.
     *
     * @param resolvedPrincipal the resolved principal
     * @param tokenCredential   the token credential
     * @return the principal
     * @throws Throwable the throwable
     */
    Principal validate(Principal resolvedPrincipal, CasTwilioMultifactorTokenCredential tokenCredential) throws Throwable;

}
