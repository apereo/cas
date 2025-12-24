package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.credential.AbstractCredential;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.credentials.AnonymousCredentials;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.UserProfile;

/**
 * This class represents client credentials and (after authentication) a user profile.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@ToString
@Setter
@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuppressWarnings("NullAway.Init")
public class ClientCredential extends AbstractCredential {

    /**
     * The prefix used when building an identifier for an unauthenticated user.
     */
    public static final String NOT_YET_AUTHENTICATED = "NotYetAuthenticated-";

    /***
     * The name of the client used to perform the authentication.
     */
    public static final String AUTHENTICATION_ATTRIBUTE_CLIENT_NAME = "clientName";

    @Serial
    private static final long serialVersionUID = -7883301304291894763L;

    /**
     * The internal credentials provided by the authentication at the provider.
     */
    private final Credentials credentials;

    /**
     * Name of the client that established the credential.
     */
    private final String clientName;

    private boolean typedIdUsed = true;

    /**
     * The user profile after authentication.
     */
    private UserProfile userProfile;

    public ClientCredential(final String clientName, final UserProfile userProfile) {
        this.credentials = new AnonymousCredentials();
        this.clientName = clientName;
        this.userProfile = userProfile;
    }

    public @Nullable UserProfile getUserProfile() {
        return userProfile != null ? userProfile : Objects.requireNonNull(credentials).getUserProfile();
    }

    @Override
    public String getId() {
        val up = getUserProfile();
        if (up != null) {
            return this.typedIdUsed ? up.getTypedId() : up.getId();
        }
        return NOT_YET_AUTHENTICATED + UUID.randomUUID();
    }
}
