package org.apereo.cas.support.events.redis;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasRedisEventsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * Test cases for {@link RedisCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Redis")
@SpringBootTest(classes = {
    CasRedisEventsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.events.redis.host=localhost",
        "cas.events.redis.port=6379"
    })
@Getter
@EnabledIfListeningOnPort(port = 6379)
@EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
class RedisCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;
}
