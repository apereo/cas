package org.jasig.cas.authentication;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class UsernamePasswordCredential implements Credential, Serializable {

    /** Authentication attribute name for password. **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    private static final long serialVersionUID = -700605081472810939L;


    @NotNull
    @Size(min=1, message = "required.username")
    private String username;

    @NotNull
    @Size(min=1, message = "required.password")
    private String password;

    /** Default constructor. */
    public UsernamePasswordCredential() {}

    /**
     * Creates a new instance with the given username and password.
     *
     * @param userName Non-null user name.
     * @param password Non-null password.
     */
    public UsernamePasswordCredential(final String userName, final String password) {
        this.username = userName;
        this.password = password;
    }

    public final String getPassword() {
        return this.password;
    }


    public final void setPassword(final String password) {
        this.password = password;
    }


    public final String getUsername() {
        return this.username;
    }

    public final void setUsername(final String userName) {
        this.username = userName;
    }


    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public String toString() {
        return this.username;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UsernamePasswordCredential that = (UsernamePasswordCredential) o;

        if (password != null ? !password.equals(that.password) : that.password != null) {
            return false;
        }

        if (username != null ? !username.equals(that.username) : that.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(username)
                .append(password)
                .toHashCode();
    }

}
