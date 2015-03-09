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

import org.jasig.cas.authentication.RememberMeCredential;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.security.Key;

/**
 * Extends CookieGenerator to allow you to retrieve a value from a request.
 * The cookie is automatically marked as httpOnly, if the servlet container has support for it.
 * 
 * <p>
 * Also has support for RememberMe Services
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 *
 */
public class CookieRetrievingCookieGenerator extends CookieGenerator {

    private static final int DEFAULT_REMEMBER_ME_MAX_AGE = 7889231;

    private static final int DEFAULT_ENCRYPTION_KEY_LENGTH = 32;

    private final byte[] encryptionSeedArray;

    private final String keyManagementAlgorithmIdentifier;

    private final String contentEncryptionAlgorithmIdentifier;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /** The maximum age the cookie should be remembered for.
     * The default is three months ({@value} in seconds, according to Google) */
    private int rememberMeMaxAge = DEFAULT_REMEMBER_ME_MAX_AGE;

    /**
     * Instantiates a new cookie retrieving cookie generator.
     * Uses an encryption key length of {@link #DEFAULT_ENCRYPTION_KEY_LENGTH}
     * with {@link KeyManagementAlgorithmIdentifiers#A256KW} as the key
     * and {@link ContentEncryptionAlgorithmIdentifiers#AES_256_CBC_HMAC_SHA_512}
     * as the content encryption.
     *
     * <p>Note that in order to customize the encryption algorithms,
     * you will need to download and install the JCE Unlimited Strength Jurisdiction
     * Policy File into your Java installation.</p>
     */
    public CookieRetrievingCookieGenerator() {
        this(DEFAULT_ENCRYPTION_KEY_LENGTH, KeyManagementAlgorithmIdentifiers.A256KW,
                ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512);
    }

    /**
     * Instantiates a new Cookie retrieving cookie generator.
     *
     * @param keyLength the key length
     * @param keyManagementAlgorithmIdentifier the key management algorithm identifier
     * @param contentEncryptionAlgorithmIdentifier the content encryption algorithm identifier
     */
    public CookieRetrievingCookieGenerator(final int keyLength,
                                           final String keyManagementAlgorithmIdentifier,
                                           final String contentEncryptionAlgorithmIdentifier) {
        super();
        final Method setHttpOnlyMethod = ReflectionUtils.findMethod(Cookie.class, "setHttpOnly", boolean.class);
        if(setHttpOnlyMethod != null) {
            super.setCookieHttpOnly(true);
        } else {
            logger.debug("Cookie cannot be marked as HttpOnly; container is not using servlet 3.0.");
        }
        this.encryptionSeedArray = ByteUtil.randomBytes(keyLength);
        this.keyManagementAlgorithmIdentifier = keyManagementAlgorithmIdentifier;
        this.contentEncryptionAlgorithmIdentifier = contentEncryptionAlgorithmIdentifier;

    }
    /**
     * Adds the cookie, taking into account {@link RememberMeCredential#REQUEST_PARAMETER_REMEMBER_ME}
     * in the request.
     *
     * @param request the request
     * @param response the response
     * @param cookieValue the cookie value
     */
    public void addCookie(final HttpServletRequest request, final HttpServletResponse response, final String cookieValue) {
        final String encryptedCookieValue = encryptCookieValue(cookieValue);

        if (!StringUtils.hasText(request.getParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME))) {
            super.addCookie(response, encryptedCookieValue);
        } else {
            final Cookie cookie = createCookie(encryptedCookieValue);
            cookie.setMaxAge(this.rememberMeMaxAge);
            if (isCookieSecure()) {
                cookie.setSecure(true);
            }
            response.addCookie(cookie);
        }
    }

    /**
     * Retrieve cookie value.
     *
     * @param request the request
     * @return the string
     */
    public String retrieveCookieValue(final HttpServletRequest request) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(
                request, getCookieName());

        return cookie == null ? null : decryptCookieValue(cookie.getValue());
    }

    public void setRememberMeMaxAge(final int maxAge) {
        this.rememberMeMaxAge = maxAge;
    }

    /**
     * Encrypt cookie value based on the seed array whose length was given during init,
     * and the key and content encryption ids.
     *
     * @param cookieValue the cookie value
     * @return the encoded cookie
     */
    private String encryptCookieValue(@NotNull final String cookieValue) {
        try {
            final Key key = new AesKey(this.encryptionSeedArray);
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setPayload(cookieValue);
            jwe.setAlgorithmHeaderValue(this.keyManagementAlgorithmIdentifier);
            jwe.setEncryptionMethodHeaderParameter(this.contentEncryptionAlgorithmIdentifier);
            jwe.setKey(key);

            logger.debug("Encrypting the cookie via [{}] and [{}]", this.keyManagementAlgorithmIdentifier,
                    this.contentEncryptionAlgorithmIdentifier);

            return jwe.getCompactSerialization();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files."
                    + e.getMessage(), e);
        }
    }

    /**
     * Decrypt cookie value based on the key created during init.
     *
     * @param cookieValue the cookie value
     * @return the decrypted cookie value
     */
    private String decryptCookieValue(@NotNull final String cookieValue) {
        try {
            final Key key = new AesKey(this.encryptionSeedArray);
            final JsonWebEncryption jwe = new JsonWebEncryption();
            jwe.setKey(key);
            jwe.setCompactSerialization(cookieValue);
            logger.debug("Decrypting cookie value...");
            return jwe.getPayload();
        } catch (final Exception e) {
            throw new RuntimeException("Ensure that you have installed JCE Unlimited Strength Jurisdiction Policy Files."
                    + e.getMessage(), e);
        }
    }
}
