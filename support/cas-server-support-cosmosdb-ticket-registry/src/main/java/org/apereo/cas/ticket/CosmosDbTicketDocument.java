package org.apereo.cas.ticket;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import org.jspecify.annotations.Nullable;

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

    @JsonProperty("ttl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private @Nullable Long timeToLive;
}
