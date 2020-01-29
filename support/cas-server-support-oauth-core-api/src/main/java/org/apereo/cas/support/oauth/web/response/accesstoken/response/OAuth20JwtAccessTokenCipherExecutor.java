package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;

import lombok.NoArgsConstructor;

/**
 * This is {@link OAuth20JwtAccessTokenCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@NoArgsConstructor
public class OAuth20JwtAccessTokenCipherExecutor extends JwtTicketCipherExecutor {
    public OAuth20JwtAccessTokenCipherExecutor(final boolean encryptionEnabled,
                                               final boolean signingEnabled) {
        super(null, null, null, encryptionEnabled, signingEnabled, 0, 0);
    }

    public OAuth20JwtAccessTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                               final String alg, final boolean encryptionEnabled,
                                               final boolean signingEnabled, final int signingKeySize,
                                               final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
    }

    public OAuth20JwtAccessTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                               final String alg, final boolean encryptionEnabled, final int signingKeySize,
                                               final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, alg, encryptionEnabled,
            signingKeySize, encryptionKeySize);
    }

    public OAuth20JwtAccessTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                               final boolean encryptionEnabled, final int signingKeySize, final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled,
            signingKeySize, encryptionKeySize);
    }

    public OAuth20JwtAccessTokenCipherExecutor(final String secretKeyEncryption, final String secretKeySigning,
                                               final boolean encryptionEnabled, final boolean signingEnabled,
                                               final int signingKeySize, final int encryptionKeySize) {
        super(secretKeyEncryption, secretKeySigning, encryptionEnabled,
            signingEnabled, signingKeySize, encryptionKeySize);
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.oauth.accessToken.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.oauth.accessToken.crypto.signing.key";
    }

    @Override
    public String getName() {
        return "OAuth JWT Access Tokens";
    }
}
