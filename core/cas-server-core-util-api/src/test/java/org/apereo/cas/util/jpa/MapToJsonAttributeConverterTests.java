package org.apereo.cas.util.jpa;

import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MapToJsonAttributeConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("JDBC")
class MapToJsonAttributeConverterTests {
    @Test
    void verifyOperation() {
        val converter = new MapToJsonAttributeConverter();
        val map = new HashMap<String, List<Object>>();
        map.put("attribute1", CollectionUtils.wrapList("1", "2"));
        val results = converter.convertToDatabaseColumn(map);
        assertFalse(results.isEmpty());
        val entity = converter.convertToEntityAttribute(results);
        assertEquals(entity, map);
    }
}
