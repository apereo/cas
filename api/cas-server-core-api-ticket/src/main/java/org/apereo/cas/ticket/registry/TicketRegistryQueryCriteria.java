package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TicketRegistryQueryCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
@SuperBuilder
@ToString
public class TicketRegistryQueryCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 3295014227993873566L;

    private String type;

    private Boolean decode;

    private Long count;
}
