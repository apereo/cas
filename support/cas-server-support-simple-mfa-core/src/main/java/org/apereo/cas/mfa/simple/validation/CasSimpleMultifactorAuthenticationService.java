package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;

/**
 * This is {@link CasSimpleMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface CasSimpleMultifactorAuthenticationService {

    /**
     * Bean name.
     */
    String BEAN_NAME = "casSimpleMultifactorAuthenticationService";

    /**
     * Generate cas simple multifactor authentication ticket for the given principal and service.
     *
     * @param principal the principal
     * @param service   the service
     * @return the cas simple multifactor authentication token
     * @throws Exception the exception
     */
    CasSimpleMultifactorAuthenticationTicket generate(Principal principal, Service service) throws Exception;

    /**
     * Store the token in the underlying storage, as necessary.
     *
     * @param token the token
     * @throws Exception the exception
     */
    void store(CasSimpleMultifactorAuthenticationTicket token) throws Exception;

    /**
     * Validate MFA the credential.
     *
     * @param authentication the authentication
     * @param credential     the credential
     * @return principal object representing the mfa session.
     * @throws Exception the exception
     */
    Principal validate(Authentication authentication, CasSimpleMultifactorTokenCredential credential) throws Exception;
}
