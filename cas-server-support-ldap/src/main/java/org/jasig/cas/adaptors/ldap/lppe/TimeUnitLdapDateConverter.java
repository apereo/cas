/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.adaptors.ldap.lppe;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

/**
 * An implementation the {@link LdapDateConverter} that expects the received
 * to be defined in time units specified by {@link #setTimeUnit(TimeUnit)}.
 *
 * @author Misagh Moayyed
 * @version 4.0.0
 */
public class TimeUnitLdapDateConverter extends AbstractLdapDateConverter {

    @NotNull
    private TimeUnit timeUnit = TimeUnit.DAYS;

    private DateTime sinceDateTime = null;

    /**
     * Instantiates a new time unit ldap date converter.
     */
    public TimeUnitLdapDateConverter() {
        
    }

    /**
     * Instantiates a new time unit ldap date converter.
     *
     * @param timeUnit the time unit
     */
    public TimeUnitLdapDateConverter(final TimeUnit timeUnit) {
        setTimeUnit(timeUnit);
    }

    /**
     * Instantiates a new time unit ldap date converter.
     *
     * @param timeUnit the time unit
     * @param sinceDateTime the since date time
     */
    public TimeUnitLdapDateConverter(final TimeUnit timeUnit, final DateTime sinceDateTime) {
        this(timeUnit);
        setSinceDateTime(sinceDateTime);
    }

    public void setTimeUnit(final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public void setSinceDateTime(final DateTime sinceDateTime) {
        this.sinceDateTime = sinceDateTime;
    }

    /**
     * {@inheritDoc}
     *
     * Will convert the received ldap date value into milliseconds, based on the timeUnit specified by
     * {@link #setTimeUnit(TimeUnit)}.
     * @return If {@link #sinceDateTime} is specified, will return a {@link DateTime} instance since that
     *         date plus the converted time unit to milliseconds. Otherwise, will calculate a {@link DateTime}
     *         instance since the Java epoch plus the converted time unit to milliseconds.
     */
    @Override
    public DateTime convert(final String dateValue) {
        final long longDate = Long.parseLong(dateValue);
        final Long dateInMillis = TimeUnit.MILLISECONDS.convert(longDate, this.timeUnit);

        if (sinceDateTime == null) {
            return new DateTime(dateInMillis, this.getTimeZone());
        }
        return sinceDateTime.plusMillis(dateInMillis.intValue());
    }

}
