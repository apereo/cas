package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * An extension point to the Authentication process that allows CAS to provide
 * additional attributes related to the overall Authentication (such as
 * authentication type) that are specific to the Authentication request versus
 * the Principal itself.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public interface AuthenticationMetaDataPopulator extends Ordered {

    /**
     * Adds authentication metadata attributes on successful authentication of the given credential.
     *
     * @param builder     Builder object that temporarily holds authentication metadata.
     * @param transaction The authentication transaction.
     */
    void populateAttributes(AuthenticationBuilder builder, AuthenticationTransaction transaction);

    /**
     * Determines whether the populator has the capability to perform tasks on the given credential.
     * In practice, the {@link #populateAttributes(AuthenticationBuilder, AuthenticationTransaction)} needs to be able
     * to operate on said credentials only if the return result here is {@code true}.
     *
     * @param credential The credential to check.
     * @return True if populator supports the Credential, false otherwise.
     * @since 4.1.0
     */
    boolean supports(Credential credential);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
