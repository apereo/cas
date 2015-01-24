/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.adaptors.x509.authentication.handler.support;

import org.jasig.cas.adaptors.x509.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.Calendar;


/**
 * Implements a policy to handle expired CRL data whereby expired data is permitted
 * up to a threshold period of time but not afterward.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public final class ThresholdExpiredCRLRevocationPolicy implements RevocationPolicy<X509CRL> {
    /** Default threshold is 48 hours. */
    private static final int DEFAULT_THRESHOLD = 172800;

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());


    /** Expired threshold period in seconds. */
    @Min(0)
    private int threshold = DEFAULT_THRESHOLD;


    /**
     * {@inheritDoc}
     * The CRL next update time is compared against the current time with the threshold
     * applied and rejected if and only if the next update time is in the past.
     *
     * @param crl CRL instance to evaluate.
     *
     * @throws GeneralSecurityException On expired CRL data. Check the exception type for exact details
     *
     * @see org.jasig.cas.adaptors.x509.authentication.handler.support.RevocationPolicy#apply(java.lang.Object)
     */
    @Override
    public void apply(final X509CRL crl) throws GeneralSecurityException {
        final Calendar cutoff = Calendar.getInstance();
        if (CertUtils.isExpired(crl, cutoff.getTime())) {
            cutoff.add(Calendar.SECOND, -this.threshold);
            if (CertUtils.isExpired(crl, cutoff.getTime())) {
                throw new ExpiredCRLException(crl.toString(), cutoff.getTime(), this.threshold);
            }
            logger.info(String.format("CRL expired on %s but is within threshold period, %s seconds.",
                        crl.getNextUpdate(), this.threshold));
        }
    }

    /**
     * Sets the threshold period of time after which expired CRL data is rejected.
     *
     * @param threshold Number of seconds; MUST be non-negative integer.
     */
    public void setThreshold(final int threshold) {
        this.threshold = threshold;
    }
}
