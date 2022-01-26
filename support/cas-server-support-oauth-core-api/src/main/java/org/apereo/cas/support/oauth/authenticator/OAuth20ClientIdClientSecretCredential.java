package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

/**
 * This is {@link OAuth20ClientIdClientSecretCredential}.
 * This class allows the underlying attribute repositories to make a distinction
 * between normal person-level attribute resolution requests and those that just carry client ids
 * by using a query attribute for the credential class type.
 * This allows a repository to skip or execute a request based on the credential type,
 * separating person data and client-id level attributes.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class OAuth20ClientIdClientSecretCredential extends UsernamePasswordCredential {
    private static final long serialVersionUID = 6426680333044335542L;

    public OAuth20ClientIdClientSecretCredential(final String username, final String password) {
        super(username, password);
    }
}
