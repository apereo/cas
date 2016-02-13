package org.jasig.cas.support.events.mongo;

import org.jasig.cas.mock.MockTicketGrantingTicket;
import org.jasig.cas.support.events.CasTicketGrantingTicketCreatedEvent;
import org.jasig.cas.support.events.dao.CasEventDTO;
import org.jasig.cas.support.events.dao.CasEventRepository;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test cases for {@link MongoDbCasEventRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/mongo-cloudtest-eventscontext.xml")
public class MongoDbCasEventsRepositoryTests {

    @Autowired
    private CasEventRepository repository;

    @Test
    public void verifySave() {
        final TicketGrantingTicket ticket = new MockTicketGrantingTicket("casuser");
        final CasTicketGrantingTicketCreatedEvent event = new CasTicketGrantingTicketCreatedEvent(this, ticket);

        final CasEventDTO dto = new CasEventDTO();
        dto.setType(event.getClass().getCanonicalName());
        dto.putTimestamp(event.getTimestamp());
        dto.putCreationTime(event.getTicketGrantingTicket().getCreationTime());
        dto.putId(event.getTicketGrantingTicket().getId());

        this.repository.save(dto);
    }
}
