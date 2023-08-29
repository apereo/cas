package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Locale;
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
     * The scratch codes cipher.
     */
    private final CipherExecutor<Number, Number> scratchCodesCipher;

    /**
     * Encode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount encode(final OneTimeTokenAccount account) {
        account.setSecretKey(tokenCredentialCipher.encode(account.getSecretKey()));
        account.setScratchCodes(account.getScratchCodes()
            .stream()
            .map(scratchCodesCipher::encode)
            .collect(Collectors.toList()));
        account.setUsername(account.getUsername().trim().toLowerCase(Locale.ENGLISH));
        return account;
    }


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
        val decodedScratchCodes = account.getScratchCodes()
            .stream()
            .map(code -> FunctionUtils.doAndHandle(() -> scratchCodesCipher.decode(code), t -> code).get())
            .collect(Collectors.toList());
        val newAccount = account.clone();
        newAccount.setSecretKey(decodedSecret);
        newAccount.setScratchCodes(decodedScratchCodes);
        return newAccount;
    }
}
