package org.apereo.cas.dao;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CassandraDaoTest {

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("schema.cql"), "cassandra.yaml", 120_000L);

    @Test
    public void shouldHaveLoadAnExtendDataSet() throws Exception {
        CassandraDao dao = new CassandraDao("localhost", 24, "", "", 100, new JacksonJSONSerializer());

        TicketGrantingTicketImpl tgt = defaultTGT();

        dao.addTicketGrantingTicket(tgt);

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

    private static TicketGrantingTicketImpl defaultTGT() {
        Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("something", null);
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        ArrayList<CredentialMetaData> credentials = new ArrayList<>();
        credentials.add(meta);
        Authentication defaultAuthentication = new DefaultAuthentication(ZonedDateTime.now(), credentials, NullPrincipal.getInstance(), new HashMap<>(), successes, new HashMap<>());
        return new TicketGrantingTicketImpl("id", defaultAuthentication, new TimeoutExpirationPolicy(3000));
    }
}