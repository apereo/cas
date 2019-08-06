package org.apereo.cas.web.flow.executor;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;
import org.cryptacular.bean.CipherBean;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is {@link WebflowCipherBean}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class WebflowCipherBean implements CipherBean {
    private final CipherExecutor webflowCipherExecutor;

    @Override
    public byte[] encrypt(final byte[] bytes) {
        return (byte[]) this.webflowCipherExecutor.encode(bytes);
    }

    @Override
    public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new IllegalArgumentException(
            new OperationNotSupportedException("Encrypting input stream is not supported"));
    }

    @Override
    public byte[] decrypt(final byte[] bytes) {
        return (byte[]) this.webflowCipherExecutor.decode(bytes);
    }

    @Override
    public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new IllegalArgumentException(
            new OperationNotSupportedException("Decrypting input stream is not supported"));
    }
}
