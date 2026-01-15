package org.apereo.cas.audit;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JdbcAuditTrailEntityFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("JDBC")
class JdbcAuditTrailEntityFactoryTests {
    @ParameterizedTest
    @ValueSource(strings = {"MySQL", "SQLServer", "Oracle", "PostgreSQL", "unknown", "MariaDB"})
    void verifyOperation(final String dialect) {
        val factory = new JdbcAuditTrailEntityFactory(dialect);
        assertNotNull(factory.getType());
    }
}
