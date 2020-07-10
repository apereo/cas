package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;

import com.google.common.io.ByteSource;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * Credential that are a holder for SPNEGO init token.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"initToken", "nextToken", "principal"})
public class SpnegoCredential implements Credential {

    /**
     * Unique id for serialization.
     */
    private static final long serialVersionUID = 84084596791289548L;

    private static final int NTLM_TOKEN_MAX_LENGTH = 8;

    private static final Byte CHAR_S_BYTE = (byte) 'S';

    /**
     * The ntlmssp signature.
     */
    private static final Byte[] NTLMSSP_SIGNATURE = {(byte) 'N', (byte) 'T', (byte) 'L', (byte) 'M',
        CHAR_S_BYTE, CHAR_S_BYTE, (byte) 'P', (byte) 0};

    /**
     * The SPNEGO Init Token.
     */
    @ToString.Exclude
    private byte[] initToken;

    /**
     * The SPNEGO Next Token.
     */
    @ToString.Exclude
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
    public SpnegoCredential(final @NonNull byte[] initToken) {
        this.initToken = consumeByteSourceOrNull(ByteSource.wrap(initToken));
        this.isNtlm = isTokenNtlm(this.initToken);
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
        return IntStream.range(0, NTLM_TOKEN_MAX_LENGTH).noneMatch(i -> NTLMSSP_SIGNATURE[i] != token[i]);
    }

    /**
     * Read the contents of the source into a byte array.
     *
     * @param source the byte array source
     * @return the byte[] read from the source or null
     */
    private static byte[] consumeByteSourceOrNull(final ByteSource source) {
        try {
            if (source == null || source.isEmpty()) {
                return null;
            }
            return source.read();
        } catch (final IOException e) {
            LOGGER.warn("Could not consume the byte array source", e);
            return null;
        }
    }

    @Override
    public String getId() {
        return this.principal != null ? this.principal.getId() : UNKNOWN_ID;
    }
}
