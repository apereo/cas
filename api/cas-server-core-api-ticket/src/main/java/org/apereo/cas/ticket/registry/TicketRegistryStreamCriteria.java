package org.apereo.cas.ticket.registry;

import module java.base;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

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
}
