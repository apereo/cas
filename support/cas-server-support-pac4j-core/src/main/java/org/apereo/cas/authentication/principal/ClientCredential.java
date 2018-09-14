package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Credential;
import org.pac4j.core.profile.UserProfile;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class represents client credentials and (after authentication) a user profile.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class ClientCredential implements Credential, Serializable {

    /**
     * The prefix used when building an identifier for an unauthenticated user.
     */
    public static final String NOT_YET_AUTHENTICATED = "NotYetAuthenticated-";

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -7883301304291894763L;

    private boolean typedIdUsed = true;

    /**
     * The user profile after authentication.
     */
    private UserProfile userProfile;

    /**
     * The internal credentials provided by the authentication at the provider.
     */
    private transient org.pac4j.core.credentials.Credentials credentials;

    /**
     * Define the credentials.
     *
     * @param theCredentials The authentication credentials
     */
    public ClientCredential(final org.pac4j.core.credentials.Credentials theCredentials) {
        this.credentials = theCredentials;
    }

    /**
     * Return the credentials.
     *
     * @return the credentials
     */
    public org.pac4j.core.credentials.Credentials getCredentials() {
        return this.credentials;
    }

    /**
     * Return the profile of the authenticated user.
     *
     * @return the profile of the authenticated user
     */
    public UserProfile getUserProfile() {
        return this.userProfile;
    }

    /**
     * Define the user profile.
     *
     * @param theUserProfile The user profile
     */
    public void setUserProfile(final UserProfile theUserProfile) {
        this.userProfile = theUserProfile;
    }

    @Override
    public String getId() {
        if (this.userProfile != null) {
            if (this.typedIdUsed) {
                return this.userProfile.getTypedId();
            }
            return this.userProfile.getId();
        }
        return NOT_YET_AUTHENTICATED + UUID.randomUUID().toString();
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }
}
