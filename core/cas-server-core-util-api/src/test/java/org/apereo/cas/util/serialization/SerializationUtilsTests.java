package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SerializationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
class SerializationUtilsTests {

    @Test
    void verifyOperation() {
        val result = SerializationUtils.serialize(100);
        assertThrows(ClassCastException.class,
            () -> SerializationUtils.deserializeAndCheckObject(result, String.class));
        assertThrows(ClassCastException.class,
            () -> SerializationUtils.deserialize(result, String.class));
        assertEquals(100, SerializationUtils.deserializeAndCheckObject(result, Integer.class));
        assertEquals(100, SerializationUtils.deserialize(result, Integer.class));
        assertEquals(100, SerializationUtils.deserializeAndCheckObject(result, Number.class));
        assertEquals(100, SerializationUtils.deserialize(result, Number.class));
    }

    @Test
    void verifyDeserializationFilterRejectsDisallowedClasses() throws Exception {
        val serialized = SerializationUtils.serialize(new URL("https://apereo.org"));
        val cipher = CipherExecutor.noOp();
        val thrown = assertThrows(UncheckedIOException.class,
            () -> SerializationUtils.decodeAndDeserializeObject(serialized, cipher, Serializable.class));
        assertInstanceOf(InvalidClassException.class, thrown.getCause());
    }

    @Test
    void verifyDeserializationFilterPermitsAllowedClasses() {
        val attributes = new LinkedHashMap<String, Object>();
        attributes.put("username", "casuser");
        attributes.put("loginTime", 1234567890L);
        attributes.put("remembered", Boolean.TRUE);

        val cipher = CipherExecutor.noOp();
        val encoded = SerializationUtils.serializeAndEncodeObject(cipher, attributes);
        val result = SerializationUtils.decodeAndDeserializeObject(encoded, cipher, Serializable.class);
        assertEquals(attributes, result);

        val casType = new TestCasDomainObject("casuser");
        val encodedCasType = SerializationUtils.serializeAndEncodeObject(cipher, casType);
        assertEquals(casType, SerializationUtils.decodeAndDeserializeObject(encodedCasType, cipher, Serializable.class));
    }

    /**
     * Stands in for a real {@code org.apereo.cas.**} domain object (e.g. a ticket
     * or authentication context) to verify the deserialization filter permits
     * this module's own types without pulling in another module as a test dependency.
     *
     * @param principal the principal
     */
    private record TestCasDomainObject(String principal) implements Serializable {
    }

}
