package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.services.persondir.IPersonAttributeDao;

/**
 * Resolves a {@link Principal} from a {@link Credential} using an arbitrary strategy.
 * Since a {@link Principal} requires an identifier at a minimum, the simplest strategy to produce a principal
 * is to simply copy {@link Credential#getId()} onto
 * {@link Principal#getId()}. Resolvers commonly query one or more data sources
 * to obtain attributes such as affiliations, group membership, display name, and email. The data source(s) may also
 * provide an alternate identifier mapped by the credential identifier.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @see Principal
 * @see Credential
 * @since 4.0.0
 */
public interface PrincipalResolver {

    /**
     * Resolves a principal from the given credential using an arbitrary strategy.
     * Assumes no principal is already resolved by the authentication subsystem, etc.
     *
     * @param credential Source credential.
     * @return the principal
     */
    default Principal resolve(Credential credential) {
        return resolve(credential, null, null);
    }
    
    /**
     * Resolves a principal from the given credential using an arbitrary strategy.
     * Assumes no principal is already resolved by the authentication subsystem, etc.
     *
     * @param credential Source credential.
     * @param handler    the authentication handler linked to the resolver. May be null.
     * @return the principal
     */
    default Principal resolve(Credential credential, AuthenticationHandler handler) {
        return resolve(credential, null, handler);
    }

    /**
     * Resolves a principal from the given credential using an arbitrary strategy.
     *
     * @param credential Source credential.
     * @param principal  A principal that may have been produced during the authentication process. May be null.
     * @param handler    the authentication handler linked to the resolver. May be null.
     * @return Resolved principal, or null if the principal could not be resolved.
     */
    Principal resolve(Credential credential, Principal principal, AuthenticationHandler handler);

    /**
     * Determines whether this instance supports principal resolution from the given credential. This method SHOULD
     * be called prior to {@link #resolve(Credential, Principal, AuthenticationHandler)}.
     *
     * @param credential The credential to check for support.
     * @return True if credential is supported, false otherwise.
     */
    boolean supports(Credential credential);

    /**
     * Gets attribute repository, if any.
     *
     * @return the attribute repository or null.
     * @since 5.1
     */
    IPersonAttributeDao getAttributeRepository();
}
