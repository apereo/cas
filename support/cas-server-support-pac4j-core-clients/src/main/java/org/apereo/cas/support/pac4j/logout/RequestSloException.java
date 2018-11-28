package org.apereo.cas.support.pac4j.logout;

import lombok.Getter;
import org.pac4j.core.exception.TechnicalException;

/**
 * Exception to request a SLO (inside the pac4j authentication delegation).
 *
 * @author Jerome Leleu
 * @since 5.3.6
 */
@Getter
public class RequestSloException extends TechnicalException {

    private final String key;

    private final boolean isFrontChannel;

    public RequestSloException(final String key, final boolean isFrontChannel) {
        super("Request a CAS SLO for key: " + key);
        this.key = key;
        this.isFrontChannel = isFrontChannel;
    }
}
