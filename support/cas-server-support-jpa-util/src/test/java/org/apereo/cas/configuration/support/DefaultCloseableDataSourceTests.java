package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCloseableDataSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Hibernate")
class DefaultCloseableDataSourceTests {
    @Test
    void verifyOperation() {
        val props = new Jpa()
            .setDriverClass("org.hsqldb.jdbcDriver")
            .setUser("sa")
            .setUrl("jdbc:hsqldb:mem:cas");

        val ds = JpaBeans.newDataSource(props);
        assertInstanceOf(Closeable.class, ds.getTargetDataSource());
        assertDoesNotThrow(() -> {
            ds.close();
            ds.destroy();
        });
    }

    static class Jpa extends AbstractJpaProperties {
        @Serial
        private static final long serialVersionUID = 1210163210567513705L;
    }
}
