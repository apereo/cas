package org.apereo.cas.util.serialization;

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
public class SerializationUtilsTests {

    @Test
    public void verifyOperation() {
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

}
