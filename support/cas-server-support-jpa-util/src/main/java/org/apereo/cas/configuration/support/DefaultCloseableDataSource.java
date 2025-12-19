package org.apereo.cas.configuration.support;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import module java.sql;

/**
 * This is {@link DefaultCloseableDataSource}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
@Getter
public final class DefaultCloseableDataSource implements CloseableDataSource {
    @Delegate(types = DataSource.class)
    private final DataSource targetDataSource;

    @Override
    public void close() throws IOException {
        if (this.targetDataSource instanceof final Closeable closeable) {
            closeable.close();
        }
    }
}
