package org.apereo.cas.configuration.support;

import module java.base;
import org.springframework.beans.factory.DisposableBean;
import module java.sql;

/**
 * This is {@link CloseableDataSource}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface CloseableDataSource extends DataSource, DisposableBean {
    /**
     * Close.
     *
     * @throws IOException the io exception
     */
    void close() throws IOException;

    /**
     * Gets target data source.
     *
     * @return the target data source
     */
    DataSource getTargetDataSource();

    @Override
    default void destroy() throws Exception {
        close();
    }
}
