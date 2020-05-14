package org.apereo.cas.support.events.dao;

import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.config.CasEventsInMemoryRepositoryConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link InMemoryCasEventRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasEventsInMemoryRepositoryConfiguration.class
})
@Getter
@Tag("Simple")
public class InMemoryCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
}
