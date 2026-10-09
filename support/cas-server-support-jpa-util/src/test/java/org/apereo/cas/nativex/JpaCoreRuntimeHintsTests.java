package org.apereo.cas.nativex;

import module java.base;
import module java.sql;
import org.apereo.cas.configuration.support.CloseableDataSource;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.util.jpa.MapToJsonAttributeConverter;
import org.apereo.cas.util.jpa.MultivaluedMapToJsonAttributeConverter;
import org.apereo.cas.util.jpa.StringToNumberAttributeConverter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.DecoratingProxy;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JpaCoreRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Native")
class JpaCoreRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new JpaCoreRuntimeHints().registerHints(hints, getClass().getClassLoader());
        for (val converter : List.of(MapToJsonAttributeConverter.class,
            MultivaluedMapToJsonAttributeConverter.class, StringToNumberAttributeConverter.class)) {
            assertTrue(RuntimeHintsPredicates.reflection().onConstructor(converter.getConstructor()).test(hints));
        }
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(CloseableDataSource.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(CloseableDataSource.class,
            SpringProxy.class, Advised.class, DecoratingProxy.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(PlatformTransactionManager.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(JpaPersistenceProviderConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(PersistenceProvider.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(DataSource.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(EntityManagerFactory.class).test(hints));
    }
}
