package org.apereo.cas.otp.repository.credentials;

import module java.base;
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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link BaseOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class BaseOneTimeTokenCredentialRepository implements OneTimeTokenCredentialRepository {
    private final CipherExecutor<String, String> tokenCredentialCipher;

    private final CipherExecutor<Number, Number> scratchCodesCipher;

    private final TenantExtractor tenantExtractor;

    private final Map<String, CipherExecutor> tenantTokenCredentialCiphers = new ConcurrentHashMap<>();

    /**
     * Encode a copy of the account for storage: the secret and scratch codes are encrypted and the username is
     * normalized. The given account is left untouched, so a caller can keep using it, or save it again, without
     * its values being encrypted a second time.
     *
     * @param account the account
     * @return the encoded copy
     */
    protected OneTimeTokenAccount encode(final OneTimeTokenAccount account) {
        val encoded = account.clone();
        encoded.setSecretKey(toTokenCredentialCipherExecutor(account).encode(account.getSecretKey()).toString());
        val scratchCodesCipherExecutor = toScratchCodesCipherExecutor(account);
        encoded.setScratchCodes(account.getScratchCodes()
            .stream()
            .map(code -> FunctionUtils.doAndHandle(() -> scratchCodesCipherExecutor.encode(code), t -> code).get())
            .collect(Collectors.toList()));
        encoded.setProperties(new ArrayList<>(account.getProperties()));
        encoded.setUsername(account.getUsername().trim().toLowerCase(Locale.ENGLISH));
        return encoded;
    }


    protected Collection<? extends OneTimeTokenAccount> decode(final Collection<? extends OneTimeTokenAccount> account) {
        return account.stream().map(this::decode).collect(Collectors.toList());
    }

    /**
     * Decode a copy of the account: the secret and the scratch codes are decrypted. A scratch code that cannot be
     * decrypted is left out rather than kept as it was stored, since a stored value that is not a scratch code must
     * never be offered to the validator as one. A secret that cannot be decoded (one whose signature cannot be
     * verified decodes to null) refuses the account.
     *
     * @param account the account
     * @return the one time token account
     */
    protected OneTimeTokenAccount decode(final OneTimeTokenAccount account) {
        val decodedSecret = Optional.ofNullable(toTokenCredentialCipherExecutor(account).decode(account.getSecretKey()))
            .map(Object::toString)
            .orElseThrow(() -> new IllegalStateException("Secret key of device [%s] registered to [%s] cannot be decoded"
                .formatted(account.getId(), account.getUsername())));
        val scratchCodesCipherExecutor = toScratchCodesCipherExecutor(account);
        val decodedScratchCodes = account.getScratchCodes()
            .stream()
            .map(code -> decodeScratchCode(scratchCodesCipherExecutor, code))
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
        if (decodedScratchCodes.size() < account.getScratchCodes().size()) {
            LOGGER.warn("[{}] scratch code(s) of device [{}] registered to [{}] cannot be decrypted and are ignored",
                account.getScratchCodes().size() - decodedScratchCodes.size(), account.getId(), account.getUsername());
        }
        val newAccount = account.clone();
        newAccount.setSecretKey(decodedSecret);
        newAccount.setScratchCodes(decodedScratchCodes);
        return newAccount;
    }

    private static Optional<Number> decodeScratchCode(final CipherExecutor<Number, Number> cipher, final Number code) {
        return FunctionUtils.doAndHandle(() -> Optional.ofNullable(cipher.decode(code)), _ -> Optional.<Number>empty()).get();
    }

    /**
     * The cipher for the account's secret: the tenant's own, when the tenant configures Google Authenticator,
     * otherwise the global one. A tenant's cipher is built once and reused for the same settings. Building a new one
     * on every call breaks decoding when the tenant leaves the keys blank, since each instance then generates its own
     * keys and cannot read what another instance encoded.
     *
     * @param account the account
     * @return the cipher
     */
    private CipherExecutor toTokenCredentialCipherExecutor(final OneTimeTokenAccount account) {
        if (StringUtils.isNotBlank(account.getTenant())) {
            val tenantDefinition = tenantExtractor.getTenantsManager().findTenant(account.getTenant()).orElseThrow();
            val bindingContext = tenantDefinition.bindProperties();
            if (bindingContext.containsBindingFor(GoogleAuthenticatorMultifactorProperties.class)) {
                val properties = bindingContext.value();
                val crypto = properties.getAuthn().getMfa().getGauth().getCrypto();
                if (!crypto.isEnabled()) {
                    return CipherExecutor.noOp();
                }
                val cacheKey = String.join("|", account.getTenant(), crypto.getEncryption().getKey(),
                    crypto.getSigning().getKey(), crypto.getAlg(), crypto.getStrategyType());
                return tenantTokenCredentialCiphers.computeIfAbsent(cacheKey,
                    _ -> CipherExecutorUtils.newStringCipherExecutor(crypto, OneTimeTokenAccountCipherExecutor.class));
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
