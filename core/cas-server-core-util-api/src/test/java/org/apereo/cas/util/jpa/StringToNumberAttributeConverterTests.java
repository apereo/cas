package org.apereo.cas.util.jpa;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.cipher.JasyptNumberCipherExecutor;
import org.apereo.cas.util.gen.Base64RandomStringGenerator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StringToNumberAttributeConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("JDBC")
class StringToNumberAttributeConverterTests {
    @Test
    void verifyOperationWithBigInteger() {
        val password = new Base64RandomStringGenerator(16).getNewString();
        val encryptor = new JasyptNumberCipherExecutor(password, "Test");
        val sourceNumber = RandomUtils.nextLong();
        val encoded = encryptor.encode(sourceNumber);
        val converter = new StringToNumberAttributeConverter();
        val dbValue = converter.convertToDatabaseColumn(encoded);
        assertFalse(dbValue.isEmpty());
        val attributeValue = converter.convertToEntityAttribute(dbValue);
        val decoded = encryptor.decode(attributeValue);
        assertNotNull(decoded);
        assertEquals(sourceNumber, decoded.longValue());
    }

    @Test
    void verifyOperationDefault() {
        val sourceNumber = RandomUtils.nextLong();
        val converter = new StringToNumberAttributeConverter();
        val dbValue = converter.convertToDatabaseColumn(sourceNumber);
        assertFalse(dbValue.isEmpty());
        val attributeValue = converter.convertToEntityAttribute(dbValue);
        assertEquals(sourceNumber, attributeValue);
    }
}
