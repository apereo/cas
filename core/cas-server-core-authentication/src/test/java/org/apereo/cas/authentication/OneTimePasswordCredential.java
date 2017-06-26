package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a one-time-password credential that contains an optional unique identifier and required password.
 * The primary difference between this component and {@link UsernamePasswordCredential} is that the username/ID is optional
 * in the former and requisite in the latter.
 * <p>
 * This class implements {@link CredentialMetaData} since the one-time-password is safe for long-term storage after
 * authentication. Note that metadata is stored only _after_ authentication, at which time the OTP has already
 * been consumed and by definition is no longer useful for authentication.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class OneTimePasswordCredential extends AbstractCredential {

    /** Serialization version marker. */
    private static final long serialVersionUID = 1892587671827699709L;

    /** One-time password. */
    private final String password;

    /** Optional unique identifier. */
    private String id;

    /**
     * Creates a one-time-password with just a password.
     *
     * @param password Non-null cleartext one-time password value.
     */
    public OneTimePasswordCredential(final String password) {
        if (password == null) {
            throw new IllegalArgumentException("One-time password cannot be null.");
        }
        this.password = password;
    }


    /**
     * Creates a one-time-password with unique ID and password.
     *
     * @param id Identifier that is commonly used to look up one-time password in system of record.
     * @param password Non-null cleartext one-time password value.
     */
    @JsonCreator
    public OneTimePasswordCredential(@JsonProperty("id") final String id, @JsonProperty("password") final String password) {
        this(password);
        this.id = id;
    }

    /**
     * Gets the cleartext one-time password value.
     *
     * @return Non-null one-time password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Gets the unique ID commonly used to look up a one-time password in a system of record.
     *
     * @return Possibly null unique ID.
     */
    @Override
    public String getId() {
        return this.id;
    }
}
