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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.CipherExecutor;
import org.jasig.cas.util.NoOpCipherExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * The {@link DefaultCasCookieValueManager} is responsible for...
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class DefaultCasCookieValueManager implements CookieValueManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasCookieValueManager.class);
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;

    /** The cipher exec that is responsible for encryption and signing of the cookie. */
    private final CipherExecutor<String, String> cipherExecutor;

    /**
     * Instantiates a new Cas cookie value manager.
     * Set the default cipher to do absolutely  nothing.
     */
    public DefaultCasCookieValueManager() {
        this(new NoOpCipherExecutor());
    }

    /**
     * Instantiates a new Cas cookie value manager.
     *
     * @param cipherExecutor the cipher executor
     */
    public DefaultCasCookieValueManager(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
        LOGGER.debug("Using cipher [{} to encrypt and decode the cookie",
                this.cipherExecutor.getClass());
    }

    @Override
    public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        final StringBuilder builder = new StringBuilder(givenCookieValue);

        final String remoteAddr = request.getRemoteAddr();
        if (StringUtils.isBlank(remoteAddr)) {
            throw new IllegalStateException("Request does not specify a remote address");
        }
        builder.append(COOKIE_FIELD_SEPARATOR);
        builder.append(remoteAddr);

        final String userAgent = request.getHeader("user-agent");
        if (StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Request does not specify a user-agent");
        }
        builder.append(COOKIE_FIELD_SEPARATOR);
        builder.append(userAgent);

        final String res = builder.toString();
        LOGGER.debug("Encoding cookie value [{}]", res);
        return this.cipherExecutor.encode(res);
    }

    @Override
    public String obtainCookieValue(final Cookie cookie, final HttpServletRequest request) {
        final String cookieValue = this.cipherExecutor.decode(cookie.getValue());
        LOGGER.debug("Decoded cookie value is [{}]", cookieValue);
        if (StringUtils.isBlank(cookieValue)) {
            LOGGER.debug("Retrieved decoded cookie value is blank. Failed to decode cookie [{}]", cookie.getName());
            return null;
        }

        final String[] cookieParts = cookieValue.split(String.valueOf(COOKIE_FIELD_SEPARATOR));
        if (cookieParts.length != COOKIE_FIELDS_LENGTH) {
            throw new IllegalStateException("Invalid cookie. Required fields are missing");
        }
        final String value = cookieParts[0];
        final String remoteAddr = cookieParts[1];
        final String userAgent = cookieParts[2];

        if (StringUtils.isBlank(value) || StringUtils.isBlank(remoteAddr)
                || StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Invalid cookie. Required fields are empty");
        }

        if (!remoteAddr.equals(request.getRemoteAddr())) {
            throw new IllegalStateException("Invalid cookie. Required remote address does not match "
                    + request.getRemoteAddr());
        }

        if (!userAgent.equals(request.getHeader("user-agent"))) {
            throw new IllegalStateException("Invalid cookie. Required user-agent does not match "
                    + request.getHeader("user-agent"));
        }
        return value;
    }
}
