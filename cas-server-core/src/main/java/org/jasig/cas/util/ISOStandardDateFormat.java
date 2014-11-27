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
package org.jasig.cas.util;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A fast date format based on the ISO-8601 standard.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class ISOStandardDateFormat extends FastDateFormat {

    private static final long serialVersionUID = 9196017562782775535L;

    /** The ISO date format used by this formatter. */
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    /**
     * Instantiates a new ISO standard date format
     * based on the format {@link #DATE_FORMAT}.
     */
    public ISOStandardDateFormat() {
        super(DATE_FORMAT, TimeZone.getDefault(), Locale.getDefault());
    }
    
    /**
     * Gets the current date and time
     * formatted by the pattern specified.
     *
     * @return the current date and time
     */
    public String getCurrentDateAndTime() {
        return format(new Date());
    }
}
