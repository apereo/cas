package org.apereo.cas.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

/**
 * This is {@link CassandraSessionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface CassandraSessionFactory extends AutoCloseable {
    /**
     * Max time-to-live value in seconds (10 years).
     */
    int MAX_TTL = 10 * 365 * 24 * 60 * 60;

    /**
     * Gets session.
     *
     * @return the session
     */
    CqlSession getSession();

    /**
     * Gets cassandra template.
     *
     * @return the cassandra template
     */
    CassandraTemplate getCassandraTemplate();

    /**
     * Gets cql template.
     *
     * @return the cql template
     */
    CqlTemplate getCqlTemplate();
}
