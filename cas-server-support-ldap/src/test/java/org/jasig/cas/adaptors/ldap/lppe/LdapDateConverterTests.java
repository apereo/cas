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

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.DateTimeZoneBuilder;
import org.junit.Test;

public class LdapDateConverterTests {

    @Test
    public void testActiveDirectoryLdapDateConverter() {
        final LdapDateConverter converter = new ActiveDirectoryLdapDateConverter();
        final DateTime expected = new DateTime(2007, 6, 24, 5, 57, 54, converter.getTimeZone());
        final DateTime time = converter.convert("128271382742968750");
        assertEquals(expected, time);
    }
    
    @Test
    public void testSimpleDateFormatConverter() {
        final DateTimeZone zone = new DateTimeZoneBuilder().toDateTimeZone("GMT", false);
        final LdapDateConverter converter = new SimpleDateFormatLdapDateConverter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", zone);
        final DateTime expected = new DateTime(2001, 7, 4, 19, 8, 56, 235, zone);
        final DateTime time = converter.convert("2001-07-04T12:08:56.235-0700");
        assertEquals(expected, time);
    }
    
    @Test
    public void testTimeUnitConverterConverter() {
        final LdapDateConverter converter = new TimeUnitLdapDateConverter(TimeUnit.DAYS);
        final DateTime expected = new DateTime(TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS), converter.getTimeZone());
        final DateTime time = converter.convert("365");
        assertEquals(expected, time);
    }
    
}
