/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Credential that are a holder for SPNEGO init token.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
public final class SpnegoCredential implements Credential, Serializable {

    /**
     * Unique id for serialization.
     */
    private static final long serialVersionUID = 84084596791289548L;

    private static final int NTLM_TOKEN_MAX_LENGTH = 8;

    private static final Byte CHAR_S_BYTE = Byte.valueOf((byte) 'S');

    /** The ntlmssp signature. */
    private static final Byte[] NTLMSSP_SIGNATURE = {Byte.valueOf((byte) 'N'),
            Byte.valueOf((byte) 'T'), Byte.valueOf((byte) 'L'),
            Byte.valueOf((byte) 'M'), CHAR_S_BYTE, CHAR_S_BYTE,
            Byte.valueOf((byte) 'P'), Byte.valueOf((byte) 0)};

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The SPNEGO Init Token.
     */
    private final ByteSource initToken;

    /**
     * The SPNEGO Next Token.
     */
    private ByteSource nextToken;

    /**
     * The Principal.
     */
    private Principal principal;

    /**
     * The authentication type should be Kerberos or NTLM.
     */
    private final boolean isNtlm;

    /**
     * Instantiates a new SPNEGO credential.
     *
     * @param initToken the init token
     */
    public SpnegoCredential(final byte[] initToken) {
        Assert.notNull(initToken, "The initToken cannot be null.");
        this.initToken = ByteSource.wrap(initToken);
        this.isNtlm = isTokenNtlm(this.initToken);
    }

    public byte[] getInitToken() {
        return consumeByteSourceOrNull(this.initToken);
    }

    public byte[] getNextToken() {
        return consumeByteSourceOrNull(this.nextToken);
    }

    /**
     * Sets next token.
     *
     * @param nextToken the next token
     */
    public void setNextToken(final byte[] nextToken) {
        this.nextToken = ByteSource.wrap(nextToken);
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

    @Override
    public String getId() {
        return this.principal != null ? this.principal.getId() : UNKNOWN_ID;
    }

    @Override
    public String toString() {
        return getId();
    }

    /**
     * Checks if is token ntlm.
     *
     * @param tokenSource the token
     * @return true, if  token ntlm
     */
    private boolean isTokenNtlm(final ByteSource tokenSource) {


        final byte[] token = consumeByteSourceOrNull(tokenSource);
        if (token == null || token.length < NTLM_TOKEN_MAX_LENGTH) {
            return false;
        }
        for (int i = 0; i < NTLM_TOKEN_MAX_LENGTH; i++) {
            if (NTLMSSP_SIGNATURE[i].byteValue() != token[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }

        final SpnegoCredential c = (SpnegoCredential) obj;

        return Arrays.equals(this.getInitToken(), c.getInitToken())
                && this.principal.equals(c.getPrincipal())
                && Arrays.equals(this.getNextToken(), c.getNextToken());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        if (this.principal != null) {
            hash = this.principal.hashCode();
        }
        return new HashCodeBuilder().append(this.getInitToken())
            .append(this.getNextToken())
            .append(hash).toHashCode();
    }

    /**
     * Read the contents of the source into a byte array.
     * @param source  the byte array source
     * @return the byte[] read from the source or null
     */
    private byte[] consumeByteSourceOrNull(final ByteSource source) {
        try {
            if (source == null || source.isEmpty()) {
                return null;
            }
            return source.read();
        } catch (final IOException e) {
            logger.warn("Could not consume the byte array source", e);
            return null;
        }
    }
}
