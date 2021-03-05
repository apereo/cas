package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link BaseOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
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
        account.setUsername(account.getUsername().trim().toLowerCase());
        return account;
    }


    /**
     * Decode collection.
     *
     * @param account the account
     * @return the collection
     */
    protected Collection<? extends OneTimeTokenAccount> decode(final Collection<? extends OneTimeTokenAccount> account) {
        return account.stream().map(this::decode).collect(Collectors.toList());
    }

    /**
     * Decode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount decode(final OneTimeTokenAccount account) {
        val decodedSecret = tokenCredentialCipher.decode(account.getSecretKey());
        val newAccount = account.clone();
        newAccount.setSecretKey(decodedSecret);
        return newAccount;
    }
}
