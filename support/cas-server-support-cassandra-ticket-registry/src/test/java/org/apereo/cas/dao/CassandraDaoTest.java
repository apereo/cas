package org.apereo.cas.dao;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.serializer.JacksonJSONSerializer;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;
import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CassandraDaoTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldHaveLoadAnExtendDataSet() throws Exception {
        CassandraDao dao = new CassandraDao("localhost", 24, "", "", 100, new JacksonJSONSerializer());

        Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("something", null);
        Authentication defaultAuthentication = new DefaultAuthentication(ZonedDateTime.now(), NullPrincipal.getInstance(), new HashMap<>(), successes);
        TicketGrantingTicketImpl tgt = new TicketGrantingTicketImpl("id", defaultAuthentication, new TimeoutExpirationPolicy(3000));

        dao.addTicketGrantingTicket(tgt);
        cassandraUnit.session.execute("select * from cas.ticketgrantingticket;").all().forEach(row -> System.out.println("ID = " + row.getString("id")));

        assertEquals(tgt, dao.getTicketGrantingTicket("id"));
    }

    @Ignore("To be completed")
    @Test
    public void shouldWorkWithAStringSerializer() throws Exception {

    }

    @Ignore("To be completed")
    @Test
    public void shouldWorkWithABinarySerializer() throws Exception {

    }

    @Ignore("To be completed")
    @Test
    public void shouldReturnExpiredTGTs() throws Exception {

    }
}