package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class EncodedTicket implements Ticket {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodedTicket.class);
    
    private static final long serialVersionUID = -7078771807487764116L;
    private String id;

    private byte[] encodedTicket;

    /**
     * Private ctor used for serialization only.
     **/
    private EncodedTicket() {
    }

    /**
     * Creates a new encoded ticket using the given encoder to encode the given
     * source ticket.
     *
     * @param encodedTicket   the encoded ticket
     * @param encodedTicketId the encoded ticket id
     */
    public EncodedTicket(final ByteSource encodedTicket, final String encodedTicketId) {
        try {
            this.id = encodedTicketId;
            this.encodedTicket = encodedTicket.read();
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Instantiates a new Encoded ticket.
     *
     * @param encodedTicket   the encoded ticket that will be decoded from base64
     * @param encodedTicketId the encoded ticket id
     */
    @JsonCreator
    public EncodedTicket(@JsonProperty("encoded") final String encodedTicket, @JsonProperty("id") final String encodedTicketId) {
        try {
            this.id = encodedTicketId;
            this.encodedTicket = EncodingUtils.decodeBase64(encodedTicket);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @JsonIgnore
    @Override
    public int getCountOfUses() {
        LOGGER.trace("[Retrieving ticket usage count]");
        return 0;
    }

    private String getOpNotSupportedMessage(final String op) {
        return op + " operation not supported on a " + getClass().getSimpleName() + ". Ticket must be decoded first";
    }

    @JsonIgnore
    @Override
    public ExpirationPolicy getExpirationPolicy() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving expiration policy]"));
        return null;
    }

    @Override
    public String getPrefix() {
        return StringUtils.EMPTY;
    }

    @Override
    public ZonedDateTime getCreationTime() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving ticket creation time]"));
        return null;
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        LOGGER.trace(getOpNotSupportedMessage("[Retrieving parent ticket-granting ticket]"));
        return null;
    }

    /**
     * Gets an encoded version of ID of the source ticket.
     *
     * @return Encoded ticket ID.
     */
    @Override
    public String getId() {
        return this.id;
    }

    protected byte[] getEncoded() {
        return this.encodedTicket;
    }

    @JsonIgnore
    @Override
    public boolean isExpired() {
        LOGGER.trace(getOpNotSupportedMessage("[Ticket expiry checking]"));
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append(this.id).build();
    }

    @Override
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final EncodedTicket rhs = (EncodedTicket) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 133)
                .append(id)
                .toHashCode();
    }
}
