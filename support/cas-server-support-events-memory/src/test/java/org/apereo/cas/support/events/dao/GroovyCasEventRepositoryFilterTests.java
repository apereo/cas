package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.filter.GroovyCasEventRepositoryFilter;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

/**
 * This is {@link GroovyCasEventRepositoryFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Groovy")
@Import(GroovyCasEventRepositoryFilterTests.GroovyCasEventRepositoryFilterTestConfiguration.class)
public class GroovyCasEventRepositoryFilterTests extends InMemoryCasEventRepositoryTests {

    @TestConfiguration("GroovyCasEventRepositoryFilterTestConfiguration")
    @Lazy(false)
    public static class GroovyCasEventRepositoryFilterTestConfiguration {

        @Bean
        public CasEventRepositoryFilter casEventRepositoryFilter() {
            return new GroovyCasEventRepositoryFilter(new ClassPathResource("GroovyCasEventRepositoryFilter.groovy"));
        }
    }
}
