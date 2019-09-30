package org.apereo.cas.web.flow.executor;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.cryptacular.bean.CipherBean;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Encodes an object by encrypting its serialized byte stream. Details of encryption are handled by an instance of
 * {@link CipherBean}.
 * <p>
 * Optional gzip compression of the serialized byte stream before encryption is supported and enabled by default.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 6.1
 */
@Slf4j
@Setter
@NoArgsConstructor
public class EncryptedTranscoder implements Transcoder {
    /**
     * Handles encryption/decryption details.
     */
    private CipherBean cipherBean;

    /**
     * Flag to indicate whether to Gzip compression before encryption.
     */
    private boolean compression = true;


    public EncryptedTranscoder(final CipherBean cipherBean) {
        setCipherBean(cipherBean);
    }

    public byte[] encode(final Object o) throws IOException {
        if (o == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        val outBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            if (this.compression) {
                out = new ObjectOutputStream(new GZIPOutputStream(outBuffer));
            } else {
                out = new ObjectOutputStream(outBuffer);
            }
            writeObjectToOutputStream(o, out);
        } catch (final NotSerializableException e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return encrypt(outBuffer);
    }

    protected void writeObjectToOutputStream(final Object o, final ObjectOutputStream out) throws IOException {
        var object = o;
        if (AopUtils.isAopProxy(o)) {
            try {
                object = Advised.class.cast(o).getTargetSource().getTarget();
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (object == null) {
                LOGGER.error("Could not determine object [{}] from proxy", o.getClass().getSimpleName());
            }
        }
        if (object != null) {
            out.writeObject(object);
        } else {
            LOGGER.warn("Unable to write object [{}] to the output stream", o);
        }
    }

    protected byte[] encrypt(final ByteArrayOutputStream outBuffer) throws IOException {
        try {
            return cipherBean.encrypt(outBuffer.toByteArray());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Encryption error", e);
        }
    }

    public Object decode(final byte[] encoded) throws IOException {
        final byte[] data;
        try {
            data = cipherBean.decrypt(encoded);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Decryption error", e);
        }
        val inBuffer = new ByteArrayInputStream(data);
        ObjectInputStream in = null;
        try {
            if (this.compression) {
                in = new ObjectInputStream(new GZIPInputStream(inBuffer));
            } else {
                in = new ObjectInputStream(inBuffer);
            }
            return in.readObject();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new IOException("Deserialization error", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
