package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import java.util.Map;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Generates PersistentIds based on the Shibboleth algorithm.
 * The generated ids are based on a principal attribute is specified, or
 * the authenticated principal id.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ShibbolethCompatiblePersistentIdGenerator implements PersistentIdGenerator {

    private static final long serialVersionUID = 6182838799563190289L;

    private static final String CONST_SEPARATOR = "!";

    private static final int CONST_DEFAULT_SALT_COUNT = 16;

    @JsonProperty
    private String salt;

    @JsonProperty
    private String attribute;

    public ShibbolethCompatiblePersistentIdGenerator(final String salt) {
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
        final String principalId = StringUtils.isNotBlank(this.attribute) && attributes.containsKey(this.attribute) ? attributes.get(this.attribute).toString() : principal.getId();
        return generate(principalId, service.getId());
    }

}
