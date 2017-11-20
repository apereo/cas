package org.apereo.cas.util.serialization;

import org.apereo.cas.CipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This is {@link SerializationUtils}
 * that encapsulates common serialization operations
 * in one spot.
 *
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 5.0.0
 */
public final class SerializationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationUtils.class);

    private SerializationUtils() {
    }

    /**
     * Serialize an object.
     *
     * @param object The object to be serialized
     * @return the +byte[]+ containing the object
     * @since 5.0.0
     */
    public static byte[] serialize(final Serializable object) {
        final ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        serialize(object, outBytes);
        return outBytes.toByteArray();
    }

    /**
     * Serialize an object.
     *
     * @param object       The object to be serialized
     * @param outputStream The stream to receive the object
     * @since 5.0.0
     */
    public static void serialize(final Serializable object, final OutputStream outputStream) {
        try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(object);
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Deserialize an object.
     *
     * @param <T>     the type parameter
     * @param inBytes The bytes to be deserialized
     * @return the object
     * @since 5.0.0
     */
    public static <T> T deserialize(final byte[] inBytes) {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(inBytes);
        return deserialize(inputStream);
    }

    /**
     * Deserialize an object.
     *
     * @param <T>         the type parameter
     * @param inputStream The stream to be deserialized
     * @return the object
     * @since 5.0.0
     */
    public static <T> T deserialize(final InputStream inputStream) {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(inputStream);
            final T obj = (T) in.readObject();
            return obj;
        } catch (final ClassNotFoundException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    LOGGER.error("Unable to serialize", e);
                }
            }
        }
    }

    /**
     * Serialize and encode object.
     *
     * @param cipher the cipher
     * @param object the object
     * @return the byte []
     * @since 4.2
     */
    public static byte[] serializeAndEncodeObject(final CipherExecutor cipher,
                                                  final Serializable object) {
        final byte[] outBytes = serialize(object);
        return (byte[]) cipher.encode(outBytes);
    }

    /**
     * Decode and serialize object.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @param cipher the cipher
     * @param type   the type
     * @return the t
     * @since 4.2
     */
    public static <T> T decodeAndDeserializeObject(final byte[] object,
                                                   final CipherExecutor cipher,
                                                   final Class<? extends Serializable> type) {
        try {
            final byte[] decoded = (byte[]) cipher.decode(object);
            return deserializeAndCheckObject(decoded, type);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Decode and serialize object.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @param type   the type
     * @return the t
     * @since 4.2
     */
    public static <T> T deserializeAndCheckObject(final byte[] object, final Class<? extends Serializable> type) {
        final Object result = deserialize(object);
        if (!type.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Decoded object is of type " + result.getClass()
                    + " when we were expecting " + type);
        }
        return (T) result;
    }
}
