package org.apereo.cas.web.flow.executor;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

/**
 * Encodes an object by encrypting its serialized byte stream. Details of encryption are handled by an instance of
 * {@link CipherExecutor}.
 * <p>
 * Optional gzip compression of the serialized byte stream before encryption is supported and enabled by default.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 6.1
 */
@Slf4j
@RequiredArgsConstructor
public class EncryptedTranscoder implements Transcoder {
    /**
     * Restricts {@link #decode(byte[])} to the classes CAS and Spring Webflow
     * actually place into flow-execution/conversation state, rejecting everything
     * else by default. This stops known deserialization gadget chains (e.g.
     * commons-collections, AspectJ weaver) from being instantiated during
     * {@code readObject()} even if the cipher key protecting this stream is ever
     * misconfigured, leaked, or brute-forced.
     */
    private static final ObjectInputFilter DECODE_FILTER = ObjectInputFilter.Config.createFilter(
        "maxdepth=64;maxrefs=10000;maxbytes=10485760;maxarray=100000;"
            + "org.apereo.cas.**;"
            + "org.apereo.inspektr.**;"
            + "org.springframework.webflow.**;"
            + "org.springframework.binding.**;"
            + "java.lang.*;"
            + "java.util.*;"
            + "java.util.concurrent.*;"
            + "java.util.concurrent.atomic.*;"
            + "java.time.*;"
            + "java.math.*;"
            + "!*");

    /**
     * Handles encryption/decryption details.
     */
    private final CipherExecutor cipherExecutor;

    /**
     * Flag to indicate whether to Gzip compression before encryption.
     */
    private final boolean compression;

    public EncryptedTranscoder(final CipherExecutor cipherBean) {
        this(cipherBean, true);
    }

    @Override
    public byte[] encode(final Object o) throws IOException {
        if (o == null) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
        try (val outBuffer = new ByteArrayOutputStream()) {
            try (val out = this.compression
                ? new ObjectOutputStream(new GZIPOutputStream(outBuffer))
                : new ObjectOutputStream(outBuffer)) {

                writeObjectToOutputStream(o, out);
            } catch (final NotSerializableException e) {
                LoggingUtils.warn(LOGGER, e);
            }
            return encrypt(outBuffer);
        }
    }


    @Override
    @SuppressWarnings("BanSerializableRead")
    public Object decode(final byte[] encoded) throws IOException {
        val data = decrypt(encoded);
        try (val inBuffer = new ByteArrayInputStream(data);
             val in = this.compression
                 ? new ObjectInputStream(new GZIPInputStream(inBuffer))
                 : new ObjectInputStream(inBuffer)) {
            in.setObjectInputFilter(DECODE_FILTER);
            return in.readObject();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new IOException("Deserialization error", e);
        }
    }

    @SuppressWarnings("BanSerializableRead")
    protected void writeObjectToOutputStream(final Object o, final ObjectOutputStream out) throws IOException {
        var object = o;
        if (AopUtils.isAopProxy(o)) {
            try {
                object = ((Advised) o).getTargetSource().getTarget();
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            if (object == null) {
                LOGGER.error("Could not determine object [{}] from proxy",
                    Objects.requireNonNull(o).getClass().getSimpleName());
            }
        }
        if (object != null) {
            out.writeObject(object);
        } else {
            LOGGER.warn("Unable to write object [{}] to the output stream", o);
        }
    }

    protected byte[] encrypt(final ByteArrayOutputStream outBuffer) {
        return (byte[]) cipherExecutor.encode(outBuffer.toByteArray());
    }

    private byte[] decrypt(final byte[] encoded) {
        return (byte[]) cipherExecutor.decode(encoded);
    }
}
