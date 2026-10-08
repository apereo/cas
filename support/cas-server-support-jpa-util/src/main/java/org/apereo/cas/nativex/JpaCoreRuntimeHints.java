package org.apereo.cas.nativex;

import module java.base;
import module java.sql;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.util.jpa.MapToJsonAttributeConverter;
import org.apereo.cas.util.jpa.MultivaluedMapToJsonAttributeConverter;
import org.apereo.cas.util.jpa.StringToNumberAttributeConverter;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;

/**
 * This is {@link JpaCoreRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class JpaCoreRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerReflectionHints(hints, List.of(MapToJsonAttributeConverter.class,
            MultivaluedMapToJsonAttributeConverter.class, StringToNumberAttributeConverter.class));
        registerProxyHints(hints, JpaVendorAdapter.class);
        registerSpringProxyHints(hints, CloseableDataSource.class);
        registerProxyHints(hints, CloseableDataSource.class, PlatformTransactionManager.class,
            JpaPersistenceProviderConfigurer.class, PersistenceProvider.class, DataSource.class, EntityManagerFactory.class);
    }
}
