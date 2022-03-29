package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.ArrayList;
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

    private static final String SEPARATOR = ";";

    /**
     * The Token credential cipher.
     */
    private final CipherExecutor<String, String> tokenCredentialCipher;

    /**
     * Whether the scratch codes should be encoded (like the secret key).
     */
    private final boolean encodeScratchCodes;

    /**
     * Encode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount encode(final OneTimeTokenAccount account) {
        String toEncode = account.getSecretKey();
        if (encodeScratchCodes) {
            for (final Integer code : account.getScratchCodes()) {
                toEncode += SEPARATOR + code;
            }
            account.setScratchCodes(new ArrayList<>());
        }
        account.setSecretKey(tokenCredentialCipher.encode(toEncode));
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
        val decoded = tokenCredentialCipher.decode(account.getSecretKey());
        val parts = decoded.split(SEPARATOR);
        val newAccount = account.clone();
        newAccount.setSecretKey(parts[0]);
        val scratchCodes = new ArrayList<Integer>();
        for (int i = 1; i < parts.length; i++) {
            scratchCodes.add(Integer.parseInt(parts[i]));
        }
        newAccount.setScratchCodes(scratchCodes);
        return newAccount;
    }
}
