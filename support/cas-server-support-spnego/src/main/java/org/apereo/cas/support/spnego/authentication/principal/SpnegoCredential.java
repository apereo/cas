package org.apereo.cas.support.spnego.authentication.principal;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
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
public class SpnegoCredential implements Credential, Serializable {

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

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The SPNEGO Init Token.
     */
    private byte[] initToken;

    /**
     * The SPNEGO Next Token.
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

    /**
     * Instantiates a new SPNEGO credential.
     *
     * @param initToken the init token
     */
    public SpnegoCredential(final byte[] initToken) {
        Assert.notNull(initToken, "The initToken cannot be null.");
        this.initToken = consumeByteSourceOrNull(ByteSource.wrap(initToken));
        this.isNtlm = isTokenNtlm(this.initToken);
    }

    public byte[] getInitToken() {
        return this.initToken;
    }

    public byte[] getNextToken() {
        return this.nextToken;
    }

    /**
     * Sets next token.
     *
     * @param nextToken the next token
     */
    public void setNextToken(final byte[] nextToken) {
        this.nextToken = consumeByteSourceOrNull(ByteSource.wrap(nextToken));
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
     * @param token the token
     * @return true, if  token ntlm
     */
    private static boolean isTokenNtlm(final byte[] token) {
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
