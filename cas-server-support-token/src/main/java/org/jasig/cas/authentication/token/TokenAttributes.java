package org.jasig.cas.authentication.token;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link TokenAttributes}.
 * Defines the object encoded in a {@link Token}'s "credentials" property.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class TokenAttributes extends ConcurrentHashMap<String, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAttributes.class);

    private List<String> requiredTokenAttributes;
    private Map<String, String> tokenAttributesMap;

    /**
     * Initialize a {@linkplain TokenAttributes} object from a JSON
     * encoded string.
     *
     * @param tokenAttributes The token attributes
     */
    public TokenAttributes(@NotNull final Map tokenAttributes) {
        this(tokenAttributes, null, null);
    }

    /**
     * Initialize a {@linkplain TokenAttributes} object from a JSON
     * encoded string. Also, set the required attributes list and the
     * attributes mapping.
     *
     * @param tokenAttributes         token attributes retrieved
     * @param requiredTokenAttributes A list of required attribute names.
     * @param tokenAttributesMap      A map that maps incoming attribute names to properties of this object.
     */
    public TokenAttributes(@NotNull final Map tokenAttributes, final List requiredTokenAttributes, final Map tokenAttributesMap) {
        setRequiredTokenAttributes(requiredTokenAttributes);
        setTokenAttributesMap(tokenAttributesMap);
        putAll(tokenAttributes);
    }

    /**
     * Defines a list of attributes that are required to be present in the
     * "credentials" object of the decrypted {@link Token}. A call to
     * {@link TokenAttributes#isValid()} must
     * be made to determine if the attributes defined by this list are present
     * on the {@linkplain TokenAttributes} instance.
     *
     * @param attributes A {@link List} of required attributes.
     */
    public void setRequiredTokenAttributes(final List<String> attributes) {
        this.requiredTokenAttributes = attributes;
    }

    /**
     * <p>Defines a mapping of attribute names in the incoming "credentials" object
     * to properties of a {@link TokenAttributes} instance. The map structure
     * should be such that the key name is the {@linkplain TokenAttributes}
     * property name, and the value the key name of the credentials object.
     * For example:</p>
     * <p/>
     * <pre>
     *   {
     *     "firstName" : "fname",
     *     "lastName" : "lname",
     *     "email" : "email",
     *     "username" : "uname"
     *   }
     * </pre>
     * <p/>
     * <p>Note that all properties of a {@linkplain TokenAttributes} instance
     * are present in the mapping. You should do this whenever you define
     * an attribute mapping.</p>
     *
     * @param attributesMap A {@link Map} of attribute names.
     */
    public void setTokenAttributesMap(final Map<String, String> attributesMap) {
        this.tokenAttributesMap = attributesMap;
    }

    /**
     * Used to determine if the instance contains all of the
     * {@link TokenAttributes#requiredTokenAttributes}. If any of the
     * required attributes are missing, this will return {@code false}.
     *
     * @return {@code true} if all required attributes are present
     */
    public boolean isValid() {
        if (!StringUtils.isNotBlank(getUsername())) {
            LOGGER.debug("Attributes are considered invalid because no username is specified");
            return false;
        }

        if (this.requiredTokenAttributes != null && !this.requiredTokenAttributes.isEmpty()) {
            for (final String attr : this.requiredTokenAttributes) {
                if (get(attr) == null) {
                    LOGGER.warn("Invalid attribute [{}] since it has no defined value", attr);
                    return false;
                }
            }
        }
        LOGGER.debug("All required token attributes are valid.");
        return true;
    }

    public String getEmail() {
        return (String) get(getAttribute("email"));
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(final String email) {
        put(getAttribute("email"), email);
    }

    public String getFirstName() {
        return (String) get(getAttribute("firstName"));
    }

    /**
     * Sets first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(final String firstName) {
        put(getAttribute("firstName"), firstName);
    }

    public String getLastName() {
        return (String) get(getAttribute("lastName"));
    }

    /**
     * Sets last name.
     *
     * @param lastName the last name
     */
    public void setLastName(final String lastName) {
        put(getAttribute("lastName"), lastName);
    }

    public String getUsername() {
        return (String) get(getAttribute("username"));
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(final String username) {
        put(getAttribute("username"), username);
    }

    /**
     * Used to lookup the name of an attribute based on whether or not
     * there is an alternate mapping supplied by
     * {@link TokenAttributes#tokenAttributesMap}.
     *
     * @param attribute The name of the {@linkplain TokenAttributes} property to lookup.
     * @return The mapped name, or the passed in name.
     */
    private String getAttribute(final String attribute) {
        final String attributeName = attribute.trim().toLowerCase();
        LOGGER.debug("Getting token attribute [{}]", attributeName);

        if (this.tokenAttributesMap != null && !this.tokenAttributesMap.isEmpty()) {
            final String attributeValue = this.tokenAttributesMap.get(attributeName);
            if (StringUtils.isBlank(attributeValue)) {
                LOGGER.debug("Token attribute [{}] has not defined a value in the attributes map. CAS will use [{}] as the value",
                        attributeName, attributeName);
                return attributeName;
            }

            LOGGER.debug("Retrieved token attribute value from attribute map [{}]", attributeValue);
            return attributeValue;
        }
        LOGGER.debug("Token attributes map is undefined, so CAS will use [{}] instead", attributeName);
        return attributeName;
    }
}
