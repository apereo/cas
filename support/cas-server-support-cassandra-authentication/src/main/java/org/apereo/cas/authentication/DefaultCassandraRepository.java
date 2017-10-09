package org.apereo.cas.authentication;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DefaultCassandraRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultCassandraRepository implements CassandraRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCassandraRepository.class);
    private static final String SELECT_USER_BY_UID_QUERY = "SELECT * FROM %s WHERE %s = ?";

    private final Session session;
    private final PreparedStatement selectUserQuery;

    public DefaultCassandraRepository(final CassandraAuthenticationProperties cassandraProperties,
                                      final CassandraSessionFactory cassandraSessionFactory) {
        final String query = String.format(SELECT_USER_BY_UID_QUERY, cassandraProperties.getTableName(),
                cassandraProperties.getUsernameAttribute());

        this.session = cassandraSessionFactory.getSession();
        this.selectUserQuery = session.prepare(query);
    }

    @Override
    public Map<String, Object> getUser(final String uid) {
        final Map<String, Object> attributes = new HashMap<>();
        final Row row = session.execute(bind(selectUserQuery, uid)).one();
        if (row != null) {
            row.getColumnDefinitions().forEach(c -> {
                LOGGER.debug("Located attribute column [{}]", c.getName());
                attributes.put(c.getName(), row.getObject(c.getName()));
            });
        }
        return attributes;
    }

    private static BoundStatement bind(final PreparedStatement statement, final Object... params) {
        final BoundStatement boundStatement = statement.bind(params);
        LOGGER.debug("CQL: {} with parameters [{}]", statement.getQueryString(), StringUtils.join(params, ", "));
        return boundStatement;
    }
}
