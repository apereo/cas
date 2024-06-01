package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
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
@Accessors(chain = true)
public class TicketRegistryQueryCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 3295014227993873566L;

    private String type;
    
    private String id;

    private String principal;

    private boolean decode;

    private long count;
}
