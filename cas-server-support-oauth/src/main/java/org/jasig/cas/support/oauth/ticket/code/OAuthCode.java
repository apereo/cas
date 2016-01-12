package org.jasig.cas.support.oauth.ticket.code;

import org.jasig.cas.ticket.ServiceTicket;

/**
 * An OAuth code (is like a service ticket without PGT grant capability).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface OAuthCode extends ServiceTicket {

    String PREFIX = "COD";
}
