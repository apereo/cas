package org.apereo.cas.adaptors.x509.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;

/**
 * Exception describing an expired CRL condition.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Getter
@RequiredArgsConstructor
public class ExpiredCRLException extends GeneralSecurityException {

    private static final long serialVersionUID = 5157864033250359972L;

    /**
     * Identifier/name of CRL.
     */
    private final String id;

    /**
     * CRL expiration date.
     */
    private final ZonedDateTime expirationDate;

    /**
     * Leniency of expiration.
     */
    private final int leniency;

    /**
     * Creates a new instance describing a CRL that expired on the given date.
     *
     * @param identifier     Identifier or name that describes CRL.
     * @param expirationDate CRL expiration date.
     */
    public ExpiredCRLException(final String identifier, final ZonedDateTime expirationDate) {
        this(identifier, expirationDate, 0);
    }
    
    @Override
    public String getMessage() {
        if (this.leniency > 0) {
            return String.format("CRL %s expired on %s and is beyond the leniency period of %s seconds.", this.id, this.expirationDate, this.leniency);
        }
        return String.format("CRL %s expired on %s", this.id, this.expirationDate);
    }
}
