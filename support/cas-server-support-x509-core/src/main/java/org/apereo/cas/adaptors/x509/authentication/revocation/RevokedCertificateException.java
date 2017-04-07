package org.apereo.cas.adaptors.x509.authentication.revocation;

import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRLEntry;
import java.time.ZonedDateTime;


/**
 * Exception that describes a revoked X.509 certificate.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class RevokedCertificateException extends GeneralSecurityException {

    /** OID for reasonCode CRL extension. */
    public static final String CRL_REASON_OID = "2.5.29.21";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8827788431199129708L;

    /** The LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevokedCertificateException.class);

    /** CRL revocation reason codes per RFC 3280. */
    public enum Reason {
        
        /** The Unspecified. */
        Unspecified,
        
        /** The Key compromise. */
        KeyCompromise,
        
        /** The CA compromise. */
        CACompromise,
        
        /** The Affiliation changed. */
        AffiliationChanged,
        
        /** The Superseded. */
        Superseded,
        
        /** The Cessation of operation. */
        CessationOfOperation,
        
        /** The Certificate hold. */
        CertificateHold,
        
        /** The Remove from crl. */
        RemoveFromCRL,
        
        /** The Privilege withdrawn. */
        PrivilegeWithdrawn,
        
        /** The AA compromise. */
        AACompromise;

        /**
         * Convert code to reason.
         *
         * @param code the code
         * @return the reason
         */
        public static Reason fromCode(final int code) {
            final Reason[] reasons = Reason.values();

            for (int i = 0; i < reasons.length; i++) {
                if (i == code) {
                    return reasons[i];
                }
            }
            throw new IllegalArgumentException("Unknown CRL reason code.");
        }
    }

    /** The revocation date. */
    private final ZonedDateTime revocationDate;

    /** The serial. */
    private final BigInteger serial;

    /** The reason. */
    private final Reason reason;

    /**
     * Instantiates a new revoked certificate exception.
     *
     * @param revoked the revoked
     * @param serial the serial
     */
    public RevokedCertificateException(final ZonedDateTime revoked, final BigInteger serial) {
        this(revoked, serial, null);
    }

    /**
     * Instantiates a new revoked certificate exception.
     *
     * @param revoked the revoked
     * @param serial the serial
     * @param reason the reason
     */
    public RevokedCertificateException(final ZonedDateTime revoked, final BigInteger serial, final Reason reason) {
        this.revocationDate = ZonedDateTime.from(revoked);
        this.serial = serial;
        this.reason = reason;
    }

    /**
     * Instantiates a new revoked certificate exception.
     *
     * @param entry the entry
     */
    public RevokedCertificateException(final X509CRLEntry entry) {
        this(DateTimeUtils.zonedDateTimeOf(entry.getRevocationDate()), entry.getSerialNumber(), getReasonFromX509Entry(entry));
    }

    /**
     * Get reason from the x509 entry.
     * @param entry  the entry
     * @return reason or null
     */
    private static Reason getReasonFromX509Entry(final X509CRLEntry entry) {
        if (entry.hasExtensions()) {
            try {
                final int code = Integer.parseInt(
                        new String(entry.getExtensionValue(CRL_REASON_OID), "ASCII"));
                if (code < Reason.values().length) {
                    return Reason.fromCode(code);
                }
            } catch (final Exception e) {
                LOGGER.trace("An exception occurred when resolving extension value: [{}]", e.getMessage());
            }
        }
        return null;
    }

    /**
     * Gets the revocation date.
     *
     * @return Returns the revocationDate.
     */
    public ZonedDateTime getRevocationDate() {
        return this.revocationDate == null ? null : ZonedDateTime.from(this.revocationDate);
    }

    /**
     * Gets the serial.
     *
     * @return Returns the serial.
     */
    public BigInteger getSerial() {
        return this.serial;
    }

    /**
     * Gets the reason.
     *
     * @return Returns the reason.
     */
    public Reason getReason() {
        return this.reason;
    }

    @Override
    public String getMessage() {
        if (this.reason != null) {
            return String.format("Certificate %s revoked on %s for reason %s",
                    this.serial, this.revocationDate, this.reason);
        }
        return String.format("Certificate %s revoked on %s", this.serial, this.revocationDate);
    }
}
