package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.configuration.model.support.mfa.gauth.GoogleAuthenticatorMultifactorScratchCodeProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    private final CipherExecutor<String, String> tokenCredentialCipher;

    private final CipherExecutor<Number, Number> scratchCodesCipher;

    private final TenantExtractor tenantExtractor;

    /**
     * Encode.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount encode(final OneTimeTokenAccount account) {
        account.setSecretKey(toTokenCredentialCipherExecutor(account).encode(account.getSecretKey()).toString());
        val scratchCodesCipherExecutor = toScratchCodesCipherExecutor(account);
        account.setScratchCodes(account.getScratchCodes()
            .stream()
            .map(code -> FunctionUtils.doAndHandle(() -> scratchCodesCipherExecutor.encode(code), t -> code).get())
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
        val decodedSecret = toTokenCredentialCipherExecutor(account).decode(account.getSecretKey()).toString();
        val scratchCodesCipherExecutor = toScratchCodesCipherExecutor(account);
        val decodedScratchCodes = account.getScratchCodes()
            .stream()
            .map(code -> FunctionUtils.doAndHandle(() -> scratchCodesCipherExecutor.decode(code), t -> code).get())
            .collect(Collectors.toList());
        val newAccount = account.clone();
        newAccount.setSecretKey(decodedSecret);
        newAccount.setScratchCodes(decodedScratchCodes);
        return newAccount;
    }

    private CipherExecutor toTokenCredentialCipherExecutor(final OneTimeTokenAccount account) {
        if (StringUtils.isNotBlank(account.getTenant())) {
            val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(account.getTenant()).orElseThrow();
            val bindingContext = tenantDefinition.bindProperties();
            if (bindingContext.containsBindingFor(GoogleAuthenticatorMultifactorProperties.class)) {
                val properties = bindingContext.value();
                val crypto = properties.getAuthn().getMfa().getGauth().getCrypto();
                return crypto.isEnabled()
                    ? CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class)
                    : CipherExecutor.noOp();
            }
        }
        return tokenCredentialCipher;
    }

    private CipherExecutor<Number, Number> toScratchCodesCipherExecutor(final OneTimeTokenAccount account) {
        if (StringUtils.isNotBlank(account.getTenant())) {
            val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(account.getTenant()).orElseThrow();
            val bindingContext = tenantDefinition.bindProperties();
            if (bindingContext.isBound() && bindingContext.containsBindingFor(GoogleAuthenticatorMultifactorScratchCodeProperties.class)) {
                val properties = bindingContext.value();
                val scratchCodesKey = properties.getAuthn().getMfa().getGauth().getCore().getScratchCodes().getEncryption().getKey();
                if (StringUtils.isNotBlank(scratchCodesKey)) {
                    return new JasyptNumberCipherExecutor(scratchCodesKey, "googleAuthenticatorScratchCodesCipherExecutor");
                }
            }
        }
        return scratchCodesCipher;
    }
}
