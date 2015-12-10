package org.jasig.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.ByteSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component("shibbolethCompatiblePersistentIdGenerator")
public final class ShibbolethCompatiblePersistentIdGenerator implements PersistentIdGenerator {

    private static final long serialVersionUID = 6182838799563190289L;

    /** Log instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethCompatiblePersistentIdGenerator.class);

    private static final byte CONST_SEPARATOR = (byte) '!';

    private static final int CONST_DEFAULT_SALT_COUNT = 16;

    @JsonProperty
    private final String salt;

    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     * The salt is initialized to a random 16-digit alphanumeric string.
     * The generated id is pseudo-anonymous which allows it to be continually uniquely
     * identified by for a particular service.
     */
    public ShibbolethCompatiblePersistentIdGenerator() {
        this.salt = RandomStringUtils.randomAlphanumeric(CONST_DEFAULT_SALT_COUNT);
    }
    
    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     *
     * @param salt the the salt
     */
    @Autowired
    public ShibbolethCompatiblePersistentIdGenerator(@NotNull @Value("${shib.id.gen.salt:casrox}") final String salt) {
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
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA");
            final Charset charset = Charset.defaultCharset();
            md.update(service.getId().getBytes(charset));
            md.update(CONST_SEPARATOR);
            md.update(principal.getId().getBytes(charset));
            md.update(CONST_SEPARATOR);

            final String result = CompressionUtils.encodeBase64(md.digest(convertSaltToByteArray()));
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
