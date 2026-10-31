package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.jpa.ticketregistry.JpaTicketRegistryProperties;
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
        registerSerializationHints(hints, JpaTicketRegistryProperties.class, AbstractJpaProperties.class,
            ConnectionPoolingProperties.class, EncryptionRandomizedSigningJwtCryptographyProperties.class,
            EncryptionRandomizedCryptoProperties.class, SigningJwtCryptoProperties.class);
        registerProxyHints(hints, LockRepository.class);
        hints.proxies().registerJdkProxy(EntityManager.class, EntityManagerProxy.class);
        registerReflectionHints(hints, List.of(BaseTicketEntity.class, JpaTicketEntity.class, MySQLJpaTicketEntity.class,
            PostgresJpaTicketEntity.class, OracleJpaTicketEntity.class, MsSqlServerJpaTicketEntity.class));
        registerReflectionHintsForConstructors(hints, List.of(JsonType.class));
    }
}
