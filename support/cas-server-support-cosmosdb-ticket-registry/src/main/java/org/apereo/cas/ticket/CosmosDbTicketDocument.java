package org.apereo.cas.ticket;

import module java.base;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link CosmosDbTicketDocument}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 8.0.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Deprecated(since = "8.0.0", forRemoval = true)
@SuppressWarnings("NullAway.Init")
public class CosmosDbTicketDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -1741535838543052903L;

    private String prefix;

    private String id;

    private String type;

    private String principal;

    private String ticket;

    private long timeToLive;
}
