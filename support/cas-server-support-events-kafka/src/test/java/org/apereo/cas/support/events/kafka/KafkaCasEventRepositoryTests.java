package org.apereo.cas.support.events.kafka;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasKafkaEventsAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link KafkaCasEventRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Kafka")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasKafkaEventsAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
}, properties = "cas.events.kafka.bootstrap-address=localhost:9092")
@EnabledIfListeningOnPort(port = 9092)
@Getter
@EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
class KafkaCasEventRepositoryTests {

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;

    @Test
    void verifySave() throws Throwable {
        getEventRepository().removeAll();

        val dto1 = getCasEvent("casuser1");
        getEventRepository().save(dto1);

        val dto2 = getCasEvent("casuser2");
        getEventRepository().save(dto2);

        val col = getEventRepository().load().toList();
        assertTrue(col.isEmpty());
    }


    private CasEvent getCasEvent(final String user) {
        val ticket = new MockTicketGrantingTicket(user);
        val event = new CasTicketGrantingTicketCreatedEvent(this, ticket, null);
        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toInstant());
        dto.putEventId(event.getTicketGrantingTicket().getId());
        dto.putClientIpAddress("1.2.3.4");
        dto.putServerIpAddress("1.2.3.4");
        val location = new GeoLocationRequest(1234, 1234);
        location.setAccuracy("80");
        location.setTimestamp(String.valueOf(event.getTimestamp()));
        dto.putGeoLocation(location);
        dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
        return dto;
    }
}
