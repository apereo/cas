/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import com.github.inspektr.audit.annotation.Audit;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.NamedAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3.5
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {

    /** Log instance for logging events, errors, warnings, etc. */
    protected final Logger log = LoggerFactory.getLogger(AuthenticationManagerImpl.class);


    /** An array of AuthenticationAttributesPopulators. */
    @NotNull
    private List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators = new ArrayList<AuthenticationMetaDataPopulator>();

    @Audit(
        action="AUTHENTICATION",
        actionResolverName="AUTHENTICATION_RESOLVER",
        resourceResolverName="AUTHENTICATION_RESOURCE_RESOLVER")
    public final Authentication authenticate(final Credentials credentials) throws AuthenticationException {

        final Pair<AuthenticationHandler, Principal> pair = authenticateAndObtainPrincipal(credentials);

        Authentication authentication = new MutableAuthentication(pair.getSecond());

        if (pair.getFirst()instanceof NamedAuthenticationHandler) {
            final NamedAuthenticationHandler a = (NamedAuthenticationHandler) pair.getFirst();
            authentication.getAttributes().put(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, a.getName());
        }

        for (final AuthenticationMetaDataPopulator authenticationMetaDataPopulator : this.authenticationMetaDataPopulators) {
            authentication = authenticationMetaDataPopulator
                .populateAttributes(authentication, credentials);
        }

        return new ImmutableAuthentication(authentication.getPrincipal(),
            authentication.getAttributes());
    }

    /**
     * @param authenticationMetaDataPopulators the
     * authenticationMetaDataPopulators to set.
     */
    public final void setAuthenticationMetaDataPopulators(
        final List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators) {
        this.authenticationMetaDataPopulators = authenticationMetaDataPopulators;
    }

    /**
     * Follows the same rules as the "authenticate" method (i.e. should only return a fully populated object, or throw an exception)
     *
     * @param credentials the credentials to check
     * @return the pair of authentication handler and principal.  CANNOT be NULL.
     * @throws AuthenticationException if there is an error authenticating.
     */
    protected abstract Pair<AuthenticationHandler,Principal> authenticateAndObtainPrincipal(Credentials credentials) throws AuthenticationException;


    protected class Pair<A,B> {

        private final A first;

        private final B second;

        public Pair(final A first, final B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return this.first;
        }


        public B getSecond() {
            return this.second;
        }
    }

}
