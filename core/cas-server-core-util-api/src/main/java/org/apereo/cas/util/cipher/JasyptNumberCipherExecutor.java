package org.apereo.cas.util.cipher;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
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
public class JasyptNumberCipherExecutor implements CipherExecutor<BigInteger, BigInteger> {
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
    public BigInteger encode(final BigInteger value, final Object[] parameters) {
        return this.jasyptInstance.encrypt(value);
    }

    @Override
    public BigInteger decode(final BigInteger value, final Object[] parameters) {
        return this.jasyptInstance.decrypt(value);
    }
}
