package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.EncodedTicket;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.EncodingUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import java.io.Serial;

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
@NoArgsConstructor(force = true)
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor
public class DefaultEncodedTicket implements EncodedTicket {

    @Serial
    private static final long serialVersionUID = -7078771807487764116L;

    private final String id;
    
    private final byte[] encodedTicket;

    private final String prefix;

    private boolean stateless;

    private String tenantId;
    
    @Setter
    private ExpirationPolicy expirationPolicy;

    @JsonCreator
    public DefaultEncodedTicket(@JsonProperty("encoded") final String encodedTicket,
                                @JsonProperty("id") final String encodedTicketId,
                                @JsonProperty("prefix") final String prefix) {
        this.id = encodedTicketId;
        this.encodedTicket = EncodingUtils.decodeBase64(encodedTicket);
        this.prefix = prefix;
    }

    public DefaultEncodedTicket(@JsonProperty("encoded") final String encodedTicket,
                                @JsonProperty("prefix") final String prefix) {
        this.id = encodedTicket;
        this.prefix = prefix;
        this.encodedTicket = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    @Override
    @JsonIgnore
    public int compareTo(final Ticket o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public void markTicketStateless() {
        stateless = true;
    }
}
