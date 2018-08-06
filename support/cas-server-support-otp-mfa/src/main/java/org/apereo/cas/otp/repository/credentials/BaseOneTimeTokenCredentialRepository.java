package org.apereo.cas.otp.repository.credentials;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;

/**
 * This is {@link BaseOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseOneTimeTokenCredentialRepository implements OneTimeTokenCredentialRepository {
    /**
     * The Token credential cipher.
     */
    private final CipherExecutor<String, String> tokenCredentialCipher;

    /**
     * Encode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount encode(final OneTimeTokenAccount account) {
        account.setSecretKey(tokenCredentialCipher.encode(account.getSecretKey()));
        return account;
    }

    /**
     * Decode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount decode(final OneTimeTokenAccount account) {
        final String decodedSecret = tokenCredentialCipher.decode(account.getSecretKey());
        final OneTimeTokenAccount newAccount = account.clone();
        newAccount.setSecretKey(decodedSecret);
        return newAccount;
    }
}
