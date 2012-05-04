/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.authentication.principal;

import java.util.Arrays;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.spnego.util.SpnegoConstants;
import org.springframework.util.Assert;

/**
 * Credentials that are a holder for Spnego init token.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SpnegoCredentials implements Credentials {

    /**
     * Unique id for serialization.
     */
    private static final long serialVersionUID = -4908976823931528L;

    /**
     * The Spnego Init Token.
     */
    private final byte[] initToken;

    /**
     * The Spnego Next Token.
     */
    private byte[] nextToken;

    /**
     * The Principal.
     */
    private Principal principal;

    /**
     * The authentication type should be Kerberos or NTLM.
     */
    private boolean isNtlm;

    public SpnegoCredentials(final byte[] initToken) {
        Assert.notNull(initToken, "The initToken cannot be null.");
        this.initToken = initToken;
        this.isNtlm = isTokenNtlm(this.initToken);
    }

    public byte[] getInitToken() {
        return this.initToken;
    }

    public byte[] getNextToken() {
        return this.nextToken;
    }

    public void setNextToken(final byte[] nextToken) {
        this.nextToken = nextToken;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(final Principal principal) {
        this.principal = principal;
    }

    public boolean isNtlm() {
        return this.isNtlm;
    }

    private boolean isTokenNtlm(final byte[] token) {
        if (token == null || token.length < 8)
            return false;
        for (int i = 0; i < 8; i++) {
            if (SpnegoConstants.NTLMSSP_SIGNATURE[i] != token[i])
                return false;
        }
        return true;
    }
    
    public String toString() {
        return this.principal !=null ? this.principal.getId() : "unknown";
    }

    public boolean equals(final Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }

        final SpnegoCredentials c = (SpnegoCredentials) obj;

        return Arrays.equals(this.initToken, c.getInitToken())
            && this.principal.equals(c.getPrincipal())
            && Arrays.equals(this.nextToken, c.getNextToken());
    }

    public int hashCode() {
        return this.initToken.hashCode() ^ this.nextToken.hashCode() ^ this.principal.hashCode();
    }
    
}