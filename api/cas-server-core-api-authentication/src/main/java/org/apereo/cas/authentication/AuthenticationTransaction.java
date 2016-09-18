package org.apereo.cas.authentication;

import com.google.common.collect.Lists;
import org.apereo.cas.authentication.principal.Service;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This is {@link AuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class AuthenticationTransaction implements Serializable {

    private static final long serialVersionUID = 6213904009424725484L;

    private Collection<Credential> credentials;
    private Service service;

    /**
     * Instantiates a new Default authentication transaction.
     *
     * @param service     the service
     * @param credentials the credentials
     */
    protected AuthenticationTransaction(final Service service, final Collection<Credential> credentials) {
        this.credentials = credentials;
        this.service = service;
    }

    public Collection<Credential> getCredentials() {
        return this.credentials;
    }

    /**
     * Wrap credentials into an authentication transaction, as a factory method,
     * and return the final result.
     *
     * @param service     the service
     * @param credentials the credentials
     * @return the authentication transaction
     */
    public static AuthenticationTransaction wrap(final Service service, final Credential... credentials) {
        return new AuthenticationTransaction(service, sanitizeCredentials(credentials));
    }

    /**
     * Wrap credentials into an authentication transaction, as a factory method,
     * and return the final result.
     *
     * @param credentials the credentials
     * @return the authentication transaction
     */
    public static AuthenticationTransaction wrap(final Credential... credentials) {
        return wrap(null, credentials);
    }

    public Service getService() {
        return this.service;
    }

    private static Set<Credential> sanitizeCredentials(final Credential[] credentials) {
        if (credentials != null && credentials.length > 0) {
            final Set<Credential> set = new HashSet<>(Lists.newArrayList(credentials));
            final Iterator<Credential> it = set.iterator();
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                }
            }
            return set;
        }
        return Collections.emptySet();
    }
}

