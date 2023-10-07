package org.apereo.cas.support.events.dao;

import org.apereo.cas.config.CasEventsInMemoryRepositoryConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    CasEventsInMemoryRepositoryConfiguration.class
})
@Getter
@Tag("Events")
@ResourceLock(value = "eventRepository", mode = ResourceAccessMode.READ_WRITE)
class InMemoryCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;
}
