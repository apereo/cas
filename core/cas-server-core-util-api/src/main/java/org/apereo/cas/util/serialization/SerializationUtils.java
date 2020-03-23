package org.apereo.cas.util.serialization;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
@UtilityClass
public class SerializationUtils {

    /**
     * Serialize an object.
     *
     * @param object The object to be serialized
     * @return the +byte[]+ containing the object
     * @since 5.0.0
     */
    public static byte[] serialize(final Serializable object) {
        val outBytes = new ByteArrayOutputStream();
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
    @SneakyThrows
    public static void serialize(final Serializable object, final OutputStream outputStream) {
        try (val out = new ObjectOutputStream(outputStream)) {
            out.writeObject(object);
        }
    }

    /**
     * Deserialize an object.
     *
     * @param <T>     the type parameter
     * @param inBytes The bytes to be de-serialized
     * @param clazz   the clazz
     * @return the object
     * @since 5.0.0
     */
    public static <T> T deserialize(final byte[] inBytes, final Class<T> clazz) {
        val inputStream = new ByteArrayInputStream(inBytes);
        return deserialize(inputStream, clazz);
    }

    /**
     * Deserialize an object.
     *
     * @param <T>         the type parameter
     * @param inputStream The stream to be de-serialized
     * @param clazz       the clazz
     * @return the object
     * @since 5.0.0
     */
    @SneakyThrows
    public static <T> T deserialize(final InputStream inputStream, final Class<T> clazz) {
        try (val in = new ObjectInputStream(inputStream)) {
            val obj = in.readObject();

            if (!clazz.isAssignableFrom(obj.getClass())) {
                throw new ClassCastException("Result [" + obj
                    + " is of type " + obj.getClass()
                    + " when we were expecting " + clazz);
            }
            return (T) obj;
        }
    }

    /**
     * Serialize and encode object.
     *
     * @param cipher     the cipher
     * @param object     the object
     * @param parameters the parameters
     * @return the byte []
     * @since 4.2
     */
    public static byte[] serializeAndEncodeObject(final CipherExecutor cipher,
                                                  final Serializable object,
                                                  final Object[] parameters) {
        val outBytes = serialize(object);
        return (byte[]) cipher.encode(outBytes, parameters);
    }

    /**
     * Serialize and encode object byte [ ].
     *
     * @param cipher the cipher
     * @param object the object
     * @return the byte []
     */
    public static byte[] serializeAndEncodeObject(final CipherExecutor cipher,
                                                  final Serializable object) {
        return serializeAndEncodeObject(cipher, object, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Decode and serialize object.
     *
     * @param <T>        the type parameter
     * @param object     the object
     * @param cipher     the cipher
     * @param type       the type
     * @param parameters the parameters
     * @return the t
     * @since 4.2
     */
    @SneakyThrows
    public static <T extends Serializable> T decodeAndDeserializeObject(final byte[] object,
                                                                        final CipherExecutor cipher,
                                                                        final Class<T> type,
                                                                        final Object[] parameters) {
        val decoded = (byte[]) cipher.decode(object, parameters);
        return deserializeAndCheckObject(decoded, type);
    }

    /**
     * Decode and deserialize object t.
     *
     * @param <T>    the type parameter
     * @param object the object
     * @param cipher the cipher
     * @param type   the type
     * @return the t
     */
    @SneakyThrows
    public static <T extends Serializable> T decodeAndDeserializeObject(final byte[] object,
                                                                        final CipherExecutor cipher,
                                                                        final Class<T> type) {
        return decodeAndDeserializeObject(object, cipher, type, ArrayUtils.EMPTY_OBJECT_ARRAY);
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
    public static <T extends Serializable> T deserializeAndCheckObject(final byte[] object, final Class<T> type) {
        val result = deserialize(object, type);
        if (!type.isAssignableFrom(result.getClass())) {
            throw new ClassCastException("Decoded object is of type " + result.getClass() + " when we were expecting " + type);
        }
        return (T) result;
    }
}
