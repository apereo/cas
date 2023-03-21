package org.apereo.cas.support.events.mongo;



import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.MongoDbEventsConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.AbstractCasEventRepositoryTests;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.Getter;
import lombok.val;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.time.Instant;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("MongoDb")
@SpringBootTest(classes = {
        MongoDbEventsConfiguration.class,
        CasCoreHttpConfiguration.class,
        RefreshAutoConfiguration.class
},
        properties = {
                "cas.events.mongo.user-id=root",
                "cas.events.mongo.password=secret",
                "cas.events.mongo.host=localhost",
                "cas.events.mongo.port=27017",
                "cas.events.mongo.authentication-database-name=admin",
                "cas.events.mongo.database-name=events",
                "cas.events.mongo.drop-collection=true"
        })
@Getter
@EnabledIfListeningOnPort(port = 27017)
public class MongoDbCasEventRepositoryTests extends AbstractCasEventRepositoryTests {

    private static final int NUM_OF_EVENTS_LARGER_THAN_MIN_BATCH_SIZE = 103;

    @Autowired
    @Qualifier(CasEventRepository.BEAN_NAME)
    private CasEventRepository eventRepository;

    @Autowired
    @Qualifier("mongoEventsTemplate")
    private MongoTemplate mongoTemplate;

    private long currentCount;


    @BeforeEach
    public void setup() throws Exception {
        currentCount = getOpenCursorCount();
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt, null);
        for (var x = 0; x < NUM_OF_EVENTS_LARGER_THAN_MIN_BATCH_SIZE; x++) {
            val dto = prepareCasEvent(event);
            dto.setCreationTime(event.getTicketGrantingTicket().getCreationTime().toString());
            dto.putEventId(event.getTicketGrantingTicket().getId());
            dto.setPrincipalId(event.getTicketGrantingTicket().getAuthentication().getPrincipal().getId());
            eventRepository.save(dto);
        }
    }

    @Test
    public void makeSureCursorsGetClosed() throws Exception {
        val stream = eventRepository.load();
        assertNotNull(stream);
        val noValues = stream.findAny().isEmpty();
        assertFalse(noValues);
        assertEquals(currentCount, getOpenCursorCount());

    }

    @Test
    public void makeSureCursorsGetClosedIterateThroughStream() throws Exception {
        val stream = eventRepository.load();
        assertNotNull(stream);
        val aList = stream.collect(Collectors.toList());
        assertTrue(!aList.isEmpty());
        assertEquals(currentCount, getOpenCursorCount());

    }

    private CasEvent prepareCasEvent(final AbstractCasEvent event) {
        val dto = new CasEvent();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        val dt = DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp()));
        dto.setCreationTime(dt.toString());
        return dto;
    }

    public long getOpenCursorCount() {
        var cmd = new Document("serverStatus", 1);
        cmd.append("metrics", true);
        cmd.append("cursor", true);
        var result = mongoTemplate.executeCommand(cmd);
        var open = (Document) result.get("metrics", Document.class).get("cursor", Document.class).get("open", Document.class);
        return open.getLong("total");
    }


}
