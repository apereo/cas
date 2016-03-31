package org.jasig.cas.web.flow;
import org.cryptacular.bean.CipherBean;
import org.jasig.cas.CipherExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is {@link CasWebflowCipherBean}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("loginFlowCipherBean")
public class CasWebflowCipherBean implements CipherBean {
    private final CipherExecutor<byte[], byte[]> webflowCipherExecutor;

    /**
     * Instantiates a new Cas webflow cipher bean.
     *
     * @param cipherExecutor the cipher executor
     */
    @Autowired
    public CasWebflowCipherBean(@Qualifier("webflowCipherExecutor") final CipherExecutor cipherExecutor) {
        this.webflowCipherExecutor = cipherExecutor;
    }

    @Override
    public byte[] encrypt(final byte[] bytes) {
        return webflowCipherExecutor.encode(bytes);
    }

    @Override
    public void encrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new RuntimeException(new OperationNotSupportedException("Encrypting input stream is not supported"));
    }

    @Override
    public byte[] decrypt(final byte[] bytes) {
        return webflowCipherExecutor.decode(bytes);
    }

    @Override
    public void decrypt(final InputStream inputStream, final OutputStream outputStream) {
        throw new RuntimeException(new OperationNotSupportedException("Decrypting input stream is not supported"));
    }
}
