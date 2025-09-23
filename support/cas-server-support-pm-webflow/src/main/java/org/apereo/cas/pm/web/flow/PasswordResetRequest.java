package org.apereo.cas.pm.web.flow;

import org.apereo.cas.ticket.Ticket;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordResetRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@SuperBuilder
@Getter
public class PasswordResetRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6523267241310705150L;

    private final String username;

    private final Ticket passwordResetTicket;
}
