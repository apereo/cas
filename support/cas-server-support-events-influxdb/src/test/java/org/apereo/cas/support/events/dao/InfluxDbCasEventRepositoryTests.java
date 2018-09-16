package org.apereo.cas.support.events.dao;

import org.apereo.cas.category.InfluxDbCategory;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.config.CasEventsInfluxDbRepositoryConfiguration;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;

import lombok.Getter;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link InfluxDbCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasEventsInfluxDbRepositoryConfiguration.class
})
@Category(InfluxDbCategory.class)
@TestPropertySource(properties = "cas.events.influxDb.batchInterval=PT0.001S")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
@Getter
public class InfluxDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository eventRepository;
}
