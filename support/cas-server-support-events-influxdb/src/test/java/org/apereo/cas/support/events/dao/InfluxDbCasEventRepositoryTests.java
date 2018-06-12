package org.apereo.cas.support.events.dao;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.category.InfluxDbCategory;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.cas.support.events.config.CasEventsInfluxDbRepositoryConfiguration;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.junit.ConditionalIgnore;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.util.junit.RunningContinuousIntegrationCondition;
import org.apereo.cas.web.support.WebUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.*;

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
@TestPropertySource(locations = "classpath:influxdb-events.properties")
@ConditionalIgnore(condition = RunningContinuousIntegrationCondition.class)
public class InfluxDbCasEventRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("casEventRepository")
    private CasEventRepository casEventRepository;

    @Test
    public void verifyEventsStored() {
        final var dto = getCasEvent();
        casEventRepository.save(dto);
        Collection events = casEventRepository.load();
        assertFalse(events.isEmpty());
        events = casEventRepository.getEventsForPrincipal(dto.getPrincipalId());
        assertFalse(events.isEmpty());
        events = casEventRepository.getEventsOfType(dto.getType());
        assertFalse(events.isEmpty());
    }

    private CasEvent getCasEvent() {
        final var dto = new CasEvent();
        dto.setType(CasAuthenticationTransactionSuccessfulEvent.class.getCanonicalName());
        final var timestamp = new Date().getTime();
        dto.putTimestamp(timestamp);
        dto.setCreationTime(DateTimeUtils.zonedDateTimeOf(timestamp).toString());
        dto.putClientIpAddress("1.2.3.4");
        dto.putServerIpAddress("1.2.3.4");
        dto.putId("1000");
        dto.putAgent(WebUtils.getHttpServletRequestUserAgentFromRequestContext());

        final var location = new GeoLocationRequest(1234, 1234);
        dto.putGeoLocation(location);
        dto.setPrincipalId("casuser");
        return dto;
    }
}
