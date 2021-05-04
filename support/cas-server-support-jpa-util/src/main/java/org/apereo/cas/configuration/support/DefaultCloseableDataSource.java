package org.apereo.cas.configuration.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Getter
public class DefaultCloseableDataSource implements CloseableDataSource {
    @Delegate(types = DataSource.class)
    private final DataSource targetDataSource;

    @Override
    public void close() throws IOException {
        if (this.targetDataSource instanceof Closeable) {
            ((Closeable) this.targetDataSource).close();
        }
    }
}
