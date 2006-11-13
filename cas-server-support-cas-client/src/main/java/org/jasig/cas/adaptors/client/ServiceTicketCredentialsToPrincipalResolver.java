/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.client.validation.Assertion;

/**
 * Implementation of a {@link CredentialsToPrincipalResolver} that will look at
 * the {@link Assertion} returned by another CAS server.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ServiceTicketCredentialsToPrincipalResolver implements
    CredentialsToPrincipalResolver {

    public Principal resolvePrincipal(final Credentials credentials) {
        final ServiceTicketCredentials c = (ServiceTicketCredentials) credentials;
        final Assertion assertion = c.getAssertion();

        return assertion.getPrincipal();
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && ServiceTicketCredentials.class.isAssignableFrom(credentials
                .getClass());
    }
}
