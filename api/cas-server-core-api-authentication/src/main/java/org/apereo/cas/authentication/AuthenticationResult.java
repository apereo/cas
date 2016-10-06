package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;

/**
 * The {@link AuthenticationResult} is an abstraction representing final authentication outcome for any number of processed
 * authentication transactions.
 * <p>
 * An authentication result carries the primary composite authentication event, collected
 * from all authentication transactions. The principal and attributes associated with this authentication
 * are also collected out of all authentication transactions.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AuthenticationResult extends Serializable {

    /**
     * Obtains the finalized primary authentication for this result.
     *
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Gets the service for which this authentication result is relevant.
     * The service MAY be null, as an authentication result in CAS
     * can be established without providing a service/destination.
     *
     * @return the service
     */
    Service getService();

    /**
     * Indicates whether the authentication event
     * was established as part of user providing credentials.
     *
     * @return true if credentials are provided. false is authn was established as part of SSO.
     */
    boolean isCredentialProvided();

}
