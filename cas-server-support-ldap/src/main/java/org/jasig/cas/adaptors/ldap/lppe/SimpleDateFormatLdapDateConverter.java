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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SimpleDateFormatLdapDateConverter extends AbstractLdapDateConverter {

    /** The format of the date in dateFormat */
    @NotNull
    private String passwordExpirationDateFormat = null;
    
    public void setPasswordExpirationDateFormat(final String passwordExpirationDateFormat) {
        this.passwordExpirationDateFormat = passwordExpirationDateFormat;
    }

    @Override
    public DateTime convert(final String dateValue) {
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(this.passwordExpirationDateFormat);
        return new DateTime(DateTime.parse(dateValue, fmt), this.getTimeZone());
    }
}
