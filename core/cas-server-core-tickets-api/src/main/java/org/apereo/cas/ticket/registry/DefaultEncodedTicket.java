package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
@ToString(of = "id")
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
public class DefaultEncodedTicket implements EncodedTicket {

    private static final long serialVersionUID = -7078771807487764116L;

    private String id;

    private byte[] encodedTicket;

    private String prefix;

    @JsonCreator
    public DefaultEncodedTicket(@JsonProperty("encoded") final String encodedTicket,
                                @JsonProperty("id") final String encodedTicketId,
                                @JsonProperty("prefix") final String prefix) {
        this.id = encodedTicketId;
        this.encodedTicket = EncodingUtils.decodeBase64(encodedTicket);
        this.prefix = prefix;
    }

    @JsonIgnore
    @Override
    public int getCountOfUses() {
        getOpNotSupportedMessage("getCountOfUses");
        return 0;
    }

    @Override
    @JsonIgnore
    public ZonedDateTime getCreationTime() {
        getOpNotSupportedMessage("getCreationTime");
        return null;
    }

    @Override
    @JsonIgnore
    public TicketGrantingTicket getTicketGrantingTicket() {
        getOpNotSupportedMessage("getTicketGrantingTicket");
        return null;
    }

    @Override
    @JsonIgnore
    public boolean isExpired() {
        getOpNotSupportedMessage("getExpirationPolicy");
        return false;
    }

    @Override
    @JsonIgnore
    public ExpirationPolicy getExpirationPolicy() {
        getOpNotSupportedMessage("getExpirationPolicy");
        return null;
    }

    @Override
    @JsonIgnore
    public void markTicketExpired() {
    }

    @Override
    @JsonIgnore
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }

    private void getOpNotSupportedMessage(final String op) {
        LOGGER.trace("[{}] operation not supported on a [{}].", op, getClass().getSimpleName());
    }

}
