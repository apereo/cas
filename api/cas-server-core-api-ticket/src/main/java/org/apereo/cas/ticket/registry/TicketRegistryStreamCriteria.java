package org.apereo.cas.ticket.registry;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link TicketRegistryStreamCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
@SuperBuilder
@ToString
@Accessors(chain = true)
public class TicketRegistryStreamCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1385014227993873566L;

    private long from;

    @Builder.Default
    private long count = Long.MAX_VALUE;

    /**
     * When set, asks only for tickets whose stored expiration time is at or before this instant: candidates
     * for removal, not a full listing. Registries that keep no such time ignore it and return every ticket,
     * so callers must still check each ticket's expiration themselves.
     */
    private @Nullable ZonedDateTime expiringBefore;

    /**
     * Is count infinite?.
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isInfiniteCount() {
        return count == Long.MAX_VALUE || count <= 0;
    }
}
