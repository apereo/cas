package org.apereo.cas.configuration.support;

import lombok.experimental.Delegate;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

/**
 * This is {@link DefaultCloseableDataSource}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public record DefaultCloseableDataSource(@Delegate(types = DataSource.class) DataSource targetDataSource) implements CloseableDataSource {
    @Override
    public void close() throws IOException {
        if (this.targetDataSource instanceof Closeable) {
            ((Closeable) this.targetDataSource).close();
        }
    }
}
