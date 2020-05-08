package org.apereo.cas.ticket.registry;

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
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
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
@Entity
@Table(name = "ENCODEDTICKET")
public class EncodedTicket implements Ticket {

    private static final long serialVersionUID = -7078771807487764116L;

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Lob
    @Column(name = "ENCODED_TICKET", length = Integer.MAX_VALUE, nullable = false)
    private byte[] encodedTicket;

    @Column(name = "PREFIX", nullable = false)
    private String prefix;

    /**
     * Instantiates a new Encoded ticket.
     *
     * @param encodedTicket   the encoded ticket that will be decoded from base64
     * @param encodedTicketId the encoded ticket id
     * @param prefix          the ticket prefix
     */
    @SneakyThrows
    @JsonCreator
    public EncodedTicket(@JsonProperty("encoded") final String encodedTicket,
                         @JsonProperty("id") final String encodedTicketId,
                         @JsonProperty("prefix") final String prefix) {
        this.id = encodedTicketId;
        this.encodedTicket = EncodingUtils.decodeBase64(encodedTicket);
        this.prefix = prefix;
    }

    @JsonIgnore
    @Override
    public int getCountOfUses() {
        LOGGER.trace("[Retrieving ticket usage count]");
        return 0;
    }

    @JsonIgnore
    @Override
    public ExpirationPolicy getExpirationPolicy() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving expiration policy]"));
        return null;
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    @JsonIgnore
    public ZonedDateTime getCreationTime() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving ticket creation time]"));
        return null;
    }

    @Override
    @JsonIgnore
    public TicketGrantingTicket getTicketGrantingTicket() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving parent ticket-granting ticket]"));
        return null;
    }

    @JsonIgnore
    @Override
    public boolean isExpired() {
        LOGGER.trace(getOpNotSupportedMessage("[Ticket expiry checking]"));
        return false;
    }

    @Override
    @JsonIgnore
    public void markTicketExpired() {
    }

    @Override
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }

    private String getOpNotSupportedMessage(final String op) {
        return op + " operation not supported on a " + getClass().getSimpleName() + ". Ticket must be decoded first";
    }

}
