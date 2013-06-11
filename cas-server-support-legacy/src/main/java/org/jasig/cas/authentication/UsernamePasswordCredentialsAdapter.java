package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;

/**
 * Converts a CAS 4.0 username/password credential into a CAS 3.0 username/password credential.
 *
 * @author Marvin S. Addison
 * @since 4.0
 */
public class UsernamePasswordCredentialsAdapter implements CredentialsAdapter {
    @Override
    public Credentials convert(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            throw new IllegalArgumentException(credential + " not supported.");
        }
        final UsernamePasswordCredential original = (UsernamePasswordCredential) credential;
        final UsernamePasswordCredentials old = new UsernamePasswordCredentials();
        old.setUsername(original.getUsername());
        old.setPassword(original.getPassword());
        return old;
    }
}
