package org.apereo.cas.cassandra;

import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;

/**
 * This is {@link DefaultCassandraSessionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class DefaultCassandraSessionFactory implements CassandraSessionFactory, AutoCloseable, DisposableBean {

    private final CqlSession session;
    private final CassandraTemplate cassandraTemplate;
    private final CqlTemplate cqlTemplate;
    private final SSLContext sslContext;

    public DefaultCassandraSessionFactory(final BaseCassandraProperties cassandra,
                                          final SSLContext sslContext) {
        this.session = initializeCassandraSession(cassandra);
        this.cassandraTemplate = new CassandraTemplate(this.session);
        this.cqlTemplate = new CqlTemplate(this.session);
        this.sslContext = sslContext;
    }

    @Override
    public void destroy() {
        try {
            LOGGER.trace("Closing Cassandra session");
            session.close();
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        destroy();
    }

    private CqlSession initializeCassandraSession(final BaseCassandraProperties cassandra) {
        val builder = CqlSession.builder()
            .withKeyspace(cassandra.getKeyspace())
            .withAuthCredentials(cassandra.getUsername(), cassandra.getPassword());
        if (StringUtils.isNotBlank(cassandra.getLocalDc())) {
            builder.withLocalDatacenter(cassandra.getLocalDc());
        }
        builder.withSslContext(this.sslContext);
        cassandra.getContactPoints()
            .stream()
            .map(contactPoint -> {
                val hostAndPort = Splitter.on(":").splitToList(contactPoint);
                val host = hostAndPort.get(0).trim();
                val port = Integer.parseInt(hostAndPort.get(1).trim());
                return new InetSocketAddress(host, port);
            })
            .forEach(builder::addContactPoint);

        return builder.build();
    }
}
