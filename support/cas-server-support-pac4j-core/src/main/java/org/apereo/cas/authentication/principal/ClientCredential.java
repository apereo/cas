package org.apereo.cas.authentication.principal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.pac4j.core.profile.UserProfile;
import java.io.Serializable;
import lombok.ToString;

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
public class ClientCredential implements Credential, Serializable {

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
    private final org.pac4j.core.credentials.Credentials credentials;

    @Override
    public String getId() {
        if (this.userProfile != null) {
            if (this.typedIdUsed) {
                return this.userProfile.getTypedId();
            }
            return this.userProfile.getId();
        }
        return null;
    }
}
