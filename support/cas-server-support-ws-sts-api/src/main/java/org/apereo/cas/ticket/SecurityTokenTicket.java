package org.apereo.cas.ticket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;

/**
 * This is {@link SecurityTokenTicket}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface SecurityTokenTicket extends Ticket {
    /**
     * Ticket Prefix.
     */
    String PREFIX = "STS";

    /**
     * Gets security token.
     *
     * @return the security token
     */
    SecurityToken getSecurityToken();
}
