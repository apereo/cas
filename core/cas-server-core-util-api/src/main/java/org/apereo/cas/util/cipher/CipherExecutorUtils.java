package org.apereo.cas.util.cipher;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * This is {@link CipherExecutorUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class CipherExecutorUtils {


    @SneakyThrows
    public static <T extends BaseStringCipherExecutor> T newStringCipherExecutor(final EncryptionJwtSigningJwtCryptographyProperties crypto,
                                                                                 final Class<T> cipherClass) {
        val ctor = cipherClass.getDeclaredConstructor(String.class, String.class,
            String.class, int.class, int.class);
        val cipher = (T) ctor.newInstance(crypto.getEncryption().getKey(),
            crypto.getSigning().getKey(),
            crypto.getAlg(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize());
        cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
        return cipher;
    }

    @SneakyThrows
    public static <T extends BaseStringCipherExecutor> T newStringCipherExecutor(final EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto,
                                                                                 final Class<T> cipherClass) {
        val ctor = cipherClass.getDeclaredConstructor(String.class, String.class,
            String.class, boolean.class, boolean.class, int.class, int.class);
        val cipher = (T) ctor.newInstance(crypto.getEncryption().getKey(),
            crypto.getSigning().getKey(),
            crypto.getAlg(),
            crypto.isEncryptionEnabled(),
            crypto.isSigningEnabled(),
            crypto.getSigning().getKeySize(),
            crypto.getEncryption().getKeySize());
        cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
        return cipher;
    }
}
