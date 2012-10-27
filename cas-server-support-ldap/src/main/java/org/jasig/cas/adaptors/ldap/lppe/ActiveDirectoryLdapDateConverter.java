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

import org.joda.time.DateTime;

public class ActiveDirectoryLdapDateConverter extends AbstractLdapDateConverter {

    /*
     * Consider leap years, divide by 4.
     * Consider non-leap centuries, (1700,1800,1900). 2000 is a leap century
     */
    private static final long YEARS_FROM_1601_1970 = 1970 - 1601;
    private static final long TOTAL_SECONDS_FROM_1601_1970 = (YEARS_FROM_1601_1970 * 365 + YEARS_FROM_1601_1970 / 4 - 3) * 24 * 60 * 60;
    
    @Override
    public DateTime convert(final String dateValue) {
        final long l = Long.parseLong(dateValue.trim());

        final long totalSecondsSince1601 = l / 10000000;
        final long totalSecondsSince1970 = totalSecondsSince1601 - TOTAL_SECONDS_FROM_1601_1970;

        final DateTime dt = new DateTime(totalSecondsSince1970 * 1000, this.getTimeZone());
        log.debug("Recalculated ActiveDirectory's date value of {} to {}", dateValue, dt);
        return dt;
    }

}


