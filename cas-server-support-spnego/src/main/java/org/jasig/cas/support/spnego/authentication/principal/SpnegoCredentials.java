/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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