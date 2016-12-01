package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.DigestUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Generates PersistentIds based on the Shibboleth algorithm.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGenerator implements PersistentIdGenerator {

    private static final long serialVersionUID = 6182838799563190289L;

    /** Log instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethCompatiblePersistentIdGenerator.class);

    private static final String CONST_SEPARATOR = "!";

    private static final int CONST_DEFAULT_SALT_COUNT = 16;

    @JsonProperty
    private String salt;

    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     * The salt is initialized to a random 16-digit alphanumeric string.
     * The generated id is pseudo-anonymous which allows it to be continually uniquely
     * identified by for a particular service.
     */
    public ShibbolethCompatiblePersistentIdGenerator() {
        this.salt = RandomStringUtils.randomAlphanumeric(CONST_DEFAULT_SALT_COUNT);
    }

    public ShibbolethCompatiblePersistentIdGenerator(final String salt) {
        this.salt = salt;
    }

    private byte[] convertSaltToByteArray() {
        return this.salt.getBytes(Charset.defaultCharset());
    }

    /**
     * Get salt.
     *
     * @return the byte[] for the salt or null
     */
    @JsonIgnore
    public byte[] getSalt() {
        try {
            return ByteSource.wrap(convertSaltToByteArray()).read();
        } catch (final IOException e) {
            LOGGER.warn("Salt cannot be read because the byte array from source could not be consumed");
        }
        return null;
    }

    @Override
    public String generate(final Principal principal, final Service service) {
        final String data = String.join(CONST_SEPARATOR, service.getId(), principal.getId());
        final Charset charset = Charset.defaultCharset();
        final String result = EncodingUtils.encodeBase64(DigestUtils.sha(data.getBytes(charset)));
        return result.replaceAll(System.getProperty("line.separator"), StringUtils.EMPTY);
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
