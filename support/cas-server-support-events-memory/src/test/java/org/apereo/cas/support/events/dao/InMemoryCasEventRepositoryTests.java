package org.apereo.cas.support.events.dao;

import module java.base;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasEventsInMemoryRepositoryAutoConfiguration;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link InMemoryCasEventRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreScriptingAutoConfiguration.class,
    CasEventsInMemoryRepositoryAutoConfiguration.class
})
@Getter
@Tag("Events")
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "eventRepository", mode = ResourceAccessMode.READ_WRITE)
class InMemoryCasEventRepositoryTests extends AbstractCasEventRepositoryTests {
    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;
}
