package org.apereo.cas.cassandra;

import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;
import org.apereo.cas.util.LoggingUtils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import com.datastax.oss.driver.api.core.ssl.ProgrammaticSslEngineFactory;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import jakarta.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * This is {@link DefaultCassandraSessionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
public class DefaultCassandraSessionFactory implements CassandraSessionFactory, DisposableBean {

    private final CqlSession session;

    private final CassandraTemplate cassandraTemplate;

    private final CqlTemplate cqlTemplate;

    private final SSLContext sslContext;

    public DefaultCassandraSessionFactory(final BaseCassandraProperties cassandra,
                                          final SSLContext sslContext) {
        this.sslContext = sslContext;
        this.session = initializeCassandraSession(cassandra);
        this.cassandraTemplate = new CassandraTemplate(this.session);
        this.cqlTemplate = new CqlTemplate(this.session);
    }

    @Override
    public void destroy() {
        try {
            LOGGER.trace("Closing Cassandra session");
            session.close();
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
    }

    @Override
    public void close() {
        destroy();
    }

    private CqlSession initializeCassandraSession(final BaseCassandraProperties cassandra) {
        val builder = CqlSession.builder().withKeyspace(cassandra.getKeyspace());
        if (StringUtils.isNotBlank(cassandra.getUsername()) && StringUtils.isNotBlank(cassandra.getPassword())) {
            builder.withAuthCredentials(cassandra.getUsername(), cassandra.getPassword());
        }
        if (StringUtils.isNotBlank(cassandra.getLocalDc())) {
            builder.withLocalDatacenter(cassandra.getLocalDc());
        }

        val engineFactory = new ProgrammaticSslEngineFactory(this.sslContext,
            cassandra.getSslCipherSuites(), false) {
            @Nonnull
            @Override
            public SSLEngine newSslEngine(
                @Nonnull
                final EndPoint remoteEndpoint) {
                val engine = super.newSslEngine(remoteEndpoint);
                engine.setSSLParameters(buildSslParameters(cassandra));
                return engine;
            }
        };
        builder.withSslEngineFactory(engineFactory);


        val configLoader = DriverConfigLoader.programmaticBuilder()
            .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, Duration.ofSeconds(5))
            .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, Duration.ofSeconds(5))
            .withDuration(DefaultDriverOption.CONNECTION_INIT_QUERY_TIMEOUT, Duration.ofSeconds(5))
            .withDuration(DefaultDriverOption.HEARTBEAT_TIMEOUT, Duration.ofSeconds(5))
            .build();
        builder.withConfigLoader(configLoader);

        cassandra.getContactPoints()
            .stream()
            .map(contactPoint -> {
                val hostAndPort = Splitter.on(":").splitToList(contactPoint);
                val host = hostAndPort.getFirst().trim();
                val port = Integer.parseInt(hostAndPort.get(1).trim());
                return new InetSocketAddress(host, port);
            })
            .forEach(builder::addContactPoint);
        return builder.build();
    }

    protected SSLParameters buildSslParameters(final BaseCassandraProperties cassandra) {
        val parameters = new SSLParameters();
        if (cassandra.getSslProtocols() != null) {
            parameters.setProtocols(cassandra.getSslProtocols());
        }
        return parameters;
    }
}
