package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.ticket.registry.generic.BaseTicketEntity;
import org.apereo.cas.ticket.registry.generic.JpaTicketEntity;
import org.apereo.cas.ticket.registry.mssql.MsSqlServerJpaTicketEntity;
import org.apereo.cas.ticket.registry.mysql.MySQLJpaTicketEntity;
import org.apereo.cas.ticket.registry.oracle.OracleJpaTicketEntity;
import org.apereo.cas.ticket.registry.postgres.PostgresJpaTicketEntity;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.orm.jpa.EntityManagerProxy;
import jakarta.persistence.EntityManager;

/**
 * This is {@link JpaRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class JpaRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, LockRepository.class);
        hints.proxies().registerJdkProxy(EntityManager.class, EntityManagerProxy.class);
        registerReflectionHints(hints, List.of(BaseTicketEntity.class, JpaTicketEntity.class, MySQLJpaTicketEntity.class,
            PostgresJpaTicketEntity.class, OracleJpaTicketEntity.class, MsSqlServerJpaTicketEntity.class));
        registerReflectionHintsForConstructors(hints, List.of(JsonType.class));
    }
}
