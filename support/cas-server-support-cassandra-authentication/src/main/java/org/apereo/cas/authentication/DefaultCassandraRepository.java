package org.apereo.cas.authentication;

import org.apereo.cas.cassandra.CassandraSessionFactory;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link DefaultCassandraRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultCassandraRepository implements CassandraRepository {

    private final Session session;
    private final PreparedStatement selectUserQuery;

    public DefaultCassandraRepository(final CassandraAuthenticationProperties cassandraProperties, final CassandraSessionFactory cassandraSessionFactory) {
        val query = String.format(cassandraProperties.getQuery(), cassandraProperties.getTableName(), cassandraProperties.getUsernameAttribute());
        this.session = cassandraSessionFactory.getSession();
        this.selectUserQuery = session.prepare(query);
    }

    private static BoundStatement bind(final PreparedStatement statement, final Object... params) {
        val boundStatement = statement.bind(params);
        LOGGER.debug("CQL: [{}] with parameters [{}]", statement.getQueryString(), StringUtils.join(params, ", "));
        return boundStatement;
    }

    @Override
    public Map<String, List<Object>> getUser(final String uid) {
        val attributes = new HashMap<String, List<Object>>();
        val row = session.execute(bind(selectUserQuery, uid)).one();
        if (row != null) {
            row.getColumnDefinitions().forEach(c -> {
                LOGGER.debug("Located attribute column [{}]", c.getName());
                attributes.put(c.getName(), CollectionUtils.toCollection(row.getObject(c.getName()), ArrayList.class));
            });
        }
        return attributes;
    }
}
