package org.apereo.cas.util.cipher;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.experimental.UtilityClass;
import lombok.val;
import static org.jooq.lambda.Unchecked.supplier;

/**
 * This is {@link CipherExecutorUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@UtilityClass
public class CipherExecutorUtils {

    /**
     * New string cipher executor.
     *
     * @param <T>         the type parameter
     * @param crypto      the crypto
     * @param cipherClass the cipher class
     * @return the t
     */
    public static <T extends BaseStringCipherExecutor> T newStringCipherExecutor(
        final EncryptionJwtSigningJwtCryptographyProperties crypto,
        final Class<T> cipherClass) {
        return supplier(() -> {
            val ctor = cipherClass.getDeclaredConstructor(String.class, String.class,
                String.class, int.class, int.class);
            val cipher = ctor.newInstance(crypto.getEncryption().getKey(),
                crypto.getSigning().getKey(),
                crypto.getAlg(),
                crypto.getSigning().getKeySize(),
                crypto.getEncryption().getKeySize());
            cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
            return cipher;
        }).get();
    }

    /**
     * New string cipher executor.
     *
     * @param <T>         the type parameter
     * @param crypto      the crypto
     * @param cipherClass the cipher class
     * @return the t
     */
    public static <T extends BaseStringCipherExecutor> T newStringCipherExecutor(
        final EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto,
        final Class<T> cipherClass) {
        return supplier(() -> {
            val ctor = cipherClass.getDeclaredConstructor(String.class, String.class,
                String.class, boolean.class, boolean.class, int.class, int.class);
            val resolver = SpringExpressionLanguageValueResolver.getInstance();
            val cipher = ctor.newInstance(
                resolver.resolve(crypto.getEncryption().getKey()),
                resolver.resolve(crypto.getSigning().getKey()),
                resolver.resolve(crypto.getAlg()),
                crypto.isEncryptionEnabled(),
                crypto.isSigningEnabled(),
                crypto.getSigning().getKeySize(),
                crypto.getEncryption().getKeySize());
            cipher.setStrategyType(BaseStringCipherExecutor.CipherOperationsStrategyType.valueOf(crypto.getStrategyType()));
            return cipher;
        }).get();
    }
}
