package org.apereo.cas.util.cipher;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.util.numeric.AES256IntegerNumberEncryptor;

import java.math.BigInteger;
import java.security.Security;

/**
 * This is {@link JasyptNumberCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
public class JasyptNumberCipherExecutor implements CipherExecutor<Number, Number> {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final AES256IntegerNumberEncryptor jasyptInstance;

    @Getter
    private final String name;

    public JasyptNumberCipherExecutor(final String password,
                                      final String name) {
        jasyptInstance = new AES256IntegerNumberEncryptor();
        jasyptInstance.setPassword(password);
        this.name = name;
    }

    @Override
    public Number encode(final Number value, final Object[] parameters) {
        val input = new BigInteger(value.toString());
        return this.jasyptInstance.encrypt(input);
    }

    @Override
    public Number decode(final Number value, final Object[] parameters) {
        val input = new BigInteger(value.toString());
        try {
            return this.jasyptInstance.decrypt(input);
        } catch (final org.jasypt.exceptions.EncryptionOperationNotPossibleException e) {
            LOGGER.warn("Unable to decode number. Returning input value", e);
            return value;
        }
    }
}
