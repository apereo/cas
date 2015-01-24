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

import org.joda.time.DateTime;

import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * Exception describing an expired CRL condition.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public class ExpiredCRLException extends GeneralSecurityException {
    /** Serialization version marker. */
    private static final long serialVersionUID = 5157864033250359972L;

    /** Identifier/name of CRL. */
    private final String id;

    /** CRL expiration date. */
    private final DateTime expirationDate;

    /** Leniency of expiration. */
    private final int leniency;

    /**
     * Creates a new instance describing a CRL that expired on the given date.
     *
     * @param identifier Identifier or name that describes CRL.
     * @param expirationDate CRL expiration date.
     */
    public ExpiredCRLException(final String identifier, final Date expirationDate) {
        this(identifier, expirationDate, 0);
    }

    /**
     * Creates a new instance describing a CRL that expired on a date that is
     * more than leniency seconds beyond its expiration date.
     *
     * @param identifier Identifier or name that describes CRL.
     * @param expirationDate CRL expiration date.
     * @param leniency Number of seconds beyond the expiration date at which
     * the CRL is considered expired.  MUST be non-negative integer.
     */
    public ExpiredCRLException(final String identifier, final Date expirationDate, final int leniency) {
        this.id = identifier;
        this.expirationDate = new DateTime(expirationDate);
        if (leniency < 0) {
            throw new IllegalArgumentException("Leniency cannot be negative.");
        }
        this.leniency = leniency;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns the expirationDate.
     */
    public DateTime getExpirationDate() {
        return this.expirationDate == null ? null : new DateTime(this.expirationDate);
    }

    /**
     * @return Returns the leniency.
     */
    public int getLeniency() {
        return this.leniency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        if (this.leniency > 0) {
            return String.format("CRL %s expired on %s and is beyond the leniency period of %s seconds.",
                    this.id, this.expirationDate, this.leniency);
        }
        return String.format("CRL %s expired on %s", this.id, this.expirationDate);
    }
}
