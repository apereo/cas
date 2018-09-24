package org.apereo.cas.authentication.principal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.pac4j.core.profile.UserProfile;
import lombok.ToString;

import java.util.UUID;

/**
 * This class represents client credentials and (after authentication) a user profile.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class ClientCredential implements Credential {

    /**
     * The prefix used when building an identifier for an unauthenticated user.
     */
    public static final String NOT_YET_AUTHENTICATED = "NotYetAuthenticated-";

    /***
     * The name of the client used to perform the authentication.
     */
    public static final String AUTHENTICATION_ATTRIBUTE_CLIENT_NAME = "clientName";

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
    private final transient org.pac4j.core.credentials.Credentials credentials;

    /**
     * Name of the client that established the credential.
     */
    private final String clientName;

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
}
