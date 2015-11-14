package org.jasig.cas.ticket.registry.encrypt;


import com.google.common.io.ByteSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.io.IOException;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class EncodedTicket implements Ticket {

    private static final long serialVersionUID = -7078771807487764116L;
    private String id;

    private byte[] encodedTicket;

    /** Private ctor used for serialization only. **/
    private EncodedTicket() {}

    /**
     * Creates a new encoded ticket using the given encoder to encode the given
     * source ticket.
     *
     * @param encodedTicket the encoded ticket
     * @param encodedTicketId the encoded ticket id
     */
    public EncodedTicket(final ByteSource encodedTicket, final String encodedTicketId) {
        try {
            this.id = encodedTicketId;
            this.encodedTicket = encodedTicket.read();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getCountOfUses() {
        throw new UnsupportedOperationException("getCountOfUses() operation not supported");
    }

    @Override
    public long getCreationTime() {
        throw new UnsupportedOperationException("getCreationTime() operation not supported");
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        throw new UnsupportedOperationException("getGrantingTicket() operation not supported");
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

    @Override
    public boolean isExpired() {
        throw new UnsupportedOperationException("isExpired() operation not supported");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append(this.id).build();
    }
}
