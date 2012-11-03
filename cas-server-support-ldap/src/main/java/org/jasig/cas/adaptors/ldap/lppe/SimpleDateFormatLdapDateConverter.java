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

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * An implementation the {@link LdapDateConverter} that expects the received
 * date value to be consistent with a given {@link DateTimeFormatter}'s pattern.
 * 
 * @see #setPasswordExpirationDateFormat(String)
 */
public class SimpleDateFormatLdapDateConverter extends AbstractLdapDateConverter {

    @NotNull
    private String passwordExpirationDateFormat = null;
    
    private DateTimeZone timeZone = getTimeZone();
    
    public SimpleDateFormatLdapDateConverter() {}
    
    public SimpleDateFormatLdapDateConverter(final String passwordExpirationDateFormat) {
        setPasswordExpirationDateFormat(passwordExpirationDateFormat);
    }
    
    public SimpleDateFormatLdapDateConverter(final String passwordExpirationDateFormat, final DateTimeZone timeZone) {
        this(passwordExpirationDateFormat);
        this.timeZone = timeZone;
    }
    
    public void setPasswordExpirationDateFormat(final String passwordExpirationDateFormat) {
        this.passwordExpirationDateFormat = passwordExpirationDateFormat;
    }

    
    @Override
    public DateTimeZone getTimeZone() {
        return this.timeZone;
    }

    @Override
    public DateTime convert(final String dateValue) {
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(this.passwordExpirationDateFormat);
        return new DateTime(DateTime.parse(dateValue, fmt), this.getTimeZone());
    }
}
