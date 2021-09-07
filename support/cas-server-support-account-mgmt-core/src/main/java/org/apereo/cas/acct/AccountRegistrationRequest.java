package org.apereo.cas.acct;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AccountRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@NoArgsConstructor
public class AccountRegistrationRequest implements Serializable {
    private static final long serialVersionUID = -7833843820128948428L;

    @Getter
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public AccountRegistrationRequest(final Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return getProperty("username", String.class);
    }

    /**
     * Gets first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return getProperty("firstName", String.class);
    }

    /**
     * Gets last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return getProperty("lastName", String.class);
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return getProperty("email", String.class);
    }

    /**
     * Gets phone.
     *
     * @return the phone
     */
    public String getPhone() {
        return getProperty("phone", String.class);
    }

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param name  the name
     * @param clazz the clazz
     * @return the property
     */
    public <T> T getProperty(final String name, final Class<T> clazz) {
        return clazz.cast(properties.get(name));
    }

    /**
     * Contains property.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean containsProperty(final String name) {
        return properties.containsKey(name);
    }

    /**
     * Put property.
     *
     * @param name  the name
     * @param value the value
     */
    public void putProperty(final String name, final Object value) {
        this.properties.put(name, value);
    }
}
