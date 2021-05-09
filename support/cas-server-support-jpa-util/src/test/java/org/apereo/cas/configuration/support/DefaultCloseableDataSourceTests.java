package org.apereo.cas.configuration.support;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.Closeable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCloseableDataSourceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("JDBC")
public class DefaultCloseableDataSourceTests {
    @Test
    public void verifyOperation() {
        val props = new Jpa()
            .setDriverClass("org.hsqldb.jdbcDriver")
            .setUser("sa")
            .setUrl("jdbc:hsqldb:mem:cas");

        val ds = JpaBeans.newDataSource(props);
        assertTrue(ds.getTargetDataSource() instanceof Closeable);
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                ds.close();
                ds.destroy();
            }
        });
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 1210163210567513705L;
    }
}
