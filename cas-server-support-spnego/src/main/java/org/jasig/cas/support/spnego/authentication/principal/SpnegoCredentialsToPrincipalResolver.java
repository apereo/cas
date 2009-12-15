/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.authentication.principal;

import java.util.Locale;

import org.jasig.cas.authentication.principal.AbstractPersonDirectoryCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.Credentials;

import javax.validation.constraints.NotNull;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredentials and returns a SimplePrincipal.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date: 2007-06-11 11:59:18 -0400 (Mon, 11 Jun
 * 2007) $
 * @since 3.1
 */
public final class SpnegoCredentialsToPrincipalResolver extends
    AbstractPersonDirectoryCredentialsToPrincipalResolver {
    
    public static enum Transform {NONE, UPPERCASE, LOWERCASE}
    
    @NotNull
    private Transform transformPrincipalId = Transform.NONE; 

    protected String extractPrincipalId(final Credentials credentials) {
        final SpnegoCredentials c = (SpnegoCredentials) credentials;
        
        switch (this.transformPrincipalId) {
            case UPPERCASE:
                return c.getPrincipal().getId().toUpperCase(Locale.ENGLISH);
            case LOWERCASE:
                return c.getPrincipal().getId().toLowerCase(Locale.ENGLISH);
            default:
                return c.getPrincipal().getId();
        }
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && SpnegoCredentials.class.equals(credentials.getClass());
    }
    
    public void setTransformPrincipalId(final Transform transform) {
        this.transformPrincipalId = transform;
    }
}
