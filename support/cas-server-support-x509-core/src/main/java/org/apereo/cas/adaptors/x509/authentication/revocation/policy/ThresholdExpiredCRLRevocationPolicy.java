package org.apereo.cas.adaptors.x509.authentication.revocation.policy;

import org.apereo.cas.adaptors.x509.authentication.ExpiredCRLException;
import org.apereo.cas.util.crypto.CertUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.cert.X509CRL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Implements a policy to handle expired CRL data whereby expired data is permitted
 * up to a threshold period of time but not afterward.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@Slf4j
@RequiredArgsConstructor
public class ThresholdExpiredCRLRevocationPolicy implements RevocationPolicy<X509CRL> {
    /**
     * Expired threshold period in seconds after which expired CRL data is rejected.
     */
    private final int threshold;

    /**
     * {@inheritDoc}
     * The CRL next update time is compared against the current time with the threshold
     * applied and rejected if and only if the next update time is in the past.
     *
     * @param crl CRL instance to evaluate.
     * @throws ExpiredCRLException On expired CRL data. Check the exception type for exact details
     */
    @Override
    public void apply(final X509CRL crl) throws ExpiredCRLException {
        val cutoff = ZonedDateTime.now(ZoneOffset.UTC);
        if (CertUtils.isExpired(crl, cutoff)) {
            if (CertUtils.isExpired(crl, cutoff.minusSeconds(this.threshold))) {
                throw new ExpiredCRLException(crl.toString(), cutoff, this.threshold);
            }
            LOGGER.info(String.format("CRL expired on %s but is within threshold period, %s seconds.",
                crl.getNextUpdate(), this.threshold));
        }
    }
}
