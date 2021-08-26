package org.apereo.cas.token;

import org.apereo.cas.mock.MockTicketGrantingTicket;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Tickets")
public class JwtBuilderTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    public void verifyZonedDateTimeWorks() {
        val tgt = new MockTicketGrantingTicket("casuser");
        val jwt = tokenTicketBuilder.build(tgt, Map.of("date-time", List.of(ZonedDateTime.now(Clock.systemUTC()))));
        assertNotNull(jwt);
    }
}
