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
package org.jasig.cas.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SAML date utilities.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class SamlDateUtils {

    private SamlDateUtils() {
        // nothing to do
    }

    public static String getCurrentDateAndTime() {
        return getFormattedDateAndTime(new Date());
    }

    public static String getFormattedDateAndTime(final Date date) {
        final DateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
        // Google Does not set this.
        // dateFormat.setTimeZone(UTC_TIME_ZONE);
        return dateFormat.format(date);
    }
}
