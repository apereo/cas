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
package org.jasig.cas.authentication.principal;

import com.google.common.io.ByteSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates PersistentIds based on the Shibboleth algorithm.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class ShibbolethCompatiblePersistentIdGenerator implements PersistentIdGenerator {

    private static final long serialVersionUID = 6182838799563190289L;

    /** Log instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethCompatiblePersistentIdGenerator.class);

    private static final byte CONST_SEPARATOR = (byte) '!';

    private static final int CONST_DEFAULT_SALT_COUNT = 16;

    private byte[] salt;

    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     * The salt is initialized to a random 16-digit alphanumeric string.
     * The generated id is pseudo-anonymous which allows it to be continually uniquely
     * identified by for a particular service.
     */
    public ShibbolethCompatiblePersistentIdGenerator() {
        this.salt = RandomStringUtils.randomAlphanumeric(CONST_DEFAULT_SALT_COUNT).getBytes(Charset.defaultCharset());
    }
    
    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     *
     * @param salt the the salt
     */
    public ShibbolethCompatiblePersistentIdGenerator(@NotNull final String salt) {
        this.salt = salt.getBytes(Charset.defaultCharset());
    }

    /**
     * @deprecated As of 4.1.
     * Sets salt.
     *
     * @param salt the salt
     */
    @Deprecated
    public void setSalt(final String salt) {
        this.salt = salt.getBytes(Charset.defaultCharset());
        LOGGER.warn("setSalt() is deprecated and will be removed. Use the constructor instead.");
    }


    /**
     * Get salt.
     *
     * @return the byte[] for the salt or null
     */
    public byte[] getSalt() {
        try {
            return ByteSource.wrap(this.salt).read();
        } catch (final IOException e) {
            LOGGER.warn("Salt cannot be read because the byte array from source could not be consumed");
        }
        return null;
    }

    @Override
    public String generate(final Principal principal, final Service service) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(service.getId().getBytes(Charset.defaultCharset()));
            md.update(CONST_SEPARATOR);
            md.update(principal.getId().getBytes(Charset.defaultCharset()));
            md.update(CONST_SEPARATOR);

            final String result = CompressionUtils.encodeBase64(md.digest(this.salt));
            return result.replaceAll(System.getProperty("line.separator"), "");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ShibbolethCompatiblePersistentIdGenerator rhs = (ShibbolethCompatiblePersistentIdGenerator) obj;
        return new EqualsBuilder()
                .append(this.salt, rhs.salt)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.salt)
                .toHashCode();
    }
}
