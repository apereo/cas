package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AuthenticationAwareTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AuthenticationAwareTicket extends Ticket {
    /**
     * Method to retrieve the authentication.
     *
     * @return the authentication
     */
    @Nullable Authentication getAuthentication();
}
