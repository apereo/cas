package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;

/**
 * Resolves a {@link Principal} from a {@link Credential} using an arbitrary strategy.
 * Since a {@link Principal} requires an identifier at a minimum, the simplest strategy to produce a principal
 * is to simply copy {@link org.jasig.cas.authentication.Credential#getId()} onto
 * {@link org.jasig.cas.authentication.principal.Principal#getId()}. Resolvers commonly query one or more data sources
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
     *
     * @param credential Source credential.
     *
     * @return Resolved principal, or null if the principal could not be resolved.
     */
    Principal resolve(Credential credential);

    /**
     * Determines whether this instance supports principal resolution from the given credential. This method SHOULD
     * be called prior to {@link #resolve(org.jasig.cas.authentication.Credential)}.
     *
     * @param credential The credential to check for support.
     *
     * @return True if credential is supported, false otherwise.
     */
    boolean supports(Credential credential);
}
