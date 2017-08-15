package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Generates PersistentIds based on the Shibboleth algorithm.
 * The generated ids are based on a principal attribute is specified, or
 * the authenticated principal id.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGenerator implements PersistentIdGenerator {

    private static final long serialVersionUID = 6182838799563190289L;

    /**
     * Log instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethCompatiblePersistentIdGenerator.class);

    private static final String CONST_SEPARATOR = "!";

    private static final int CONST_DEFAULT_SALT_COUNT = 16;

    @JsonProperty
    private String salt;

    @JsonProperty
    private String attribute;

    /**
     * Instantiates a new shibboleth compatible persistent id generator.
     * The salt is initialized to a random alphanumeric string with length {@link #CONST_DEFAULT_SALT_COUNT}.
     * The generated id is pseudo-anonymous which allows it to be continually uniquely
     * identified by for a particular service.
     */
    public ShibbolethCompatiblePersistentIdGenerator() {
    }
    
    public ShibbolethCompatiblePersistentIdGenerator(final String salt) {
        this.salt = salt;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(final String salt) {
        this.salt = salt;
    }

    @Override
    public String generate(final String principal, final String service) {
        if (StringUtils.isBlank(salt)) {
            this.salt = new DefaultRandomStringGenerator(CONST_DEFAULT_SALT_COUNT).getNewString();
        }
        final String data = String.join(CONST_SEPARATOR, service, principal);
        final String result = StringUtils.remove(DigestUtils.shaBase64(this.salt, data, CONST_SEPARATOR), System.getProperty("line.separator"));
        LOGGER.debug("Generated persistent id for [{}] is [{}]", data, result);
        return result;
    }

    @Override
    public String generate(final Principal principal, final Service service) {
        final Map<String, Object> attributes = principal.getAttributes();
        final String principalId = StringUtils.isNotBlank(this.attribute) && attributes.containsKey(this.attribute)
                ? attributes.get(this.attribute).toString() : principal.getId();
        return generate(principalId, service.getId());
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
                .append(this.attribute, rhs.attribute)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.salt)
                .append(this.attribute)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("attribute", attribute)
                .append("salt", StringUtils.abbreviate(salt, 2))
                .toString();
    }
    
}
