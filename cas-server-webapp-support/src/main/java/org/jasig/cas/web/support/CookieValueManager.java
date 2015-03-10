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

package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * The {@link org.jasig.cas.web.support.CookieValueManager} is responsible for
 * managing all cookies and their value structure for CAS. Implementations
 * may choose to encode and sign the cookie value and optionally perform
 * additional checks to ensure the integrity of the cookie.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CookieValueManager {

    /**
     * Build cookie value.
     *
     * @param givenCookieValue the given cookie value
     * @param request the request
     * @return the original cookie value
     */
    String buildCookieValue(final String givenCookieValue, final HttpServletRequest request);

    /**
     * Obtain cookie value.
     *
     * @param cookie the cookie
     * @param request the request
     * @return the cookie value or null
     */
    String obtainCookieValue(final Cookie cookie, final HttpServletRequest request);

    /**
     * Default cookie value builder that simply returns the given cookie value
     * and does not perform any additional checks.
     * @author Misagh Moayyed
     * @since 4.1
     */
    public static class DefaultCookieValueManager implements CookieValueManager {

        @Override
        public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
            return givenCookieValue;
        }

        @Override
        public String obtainCookieValue(final Cookie cookie, final HttpServletRequest request) {
            return cookie.getValue();
        }
    }
}
