package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OAuthRefreshTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OAuthRefreshTokenProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -8328568272835831702L;

    /**
     * Hard timeout beyond which the refresh token is considered expired.
     */
    @DurationCapable
    private String timeToKillInSeconds = "P14D";

    /**
     * The storage object name used and created by CAS to hold OAuth refresh tokens
     * in the backing ticket registry implementation.
     */
    private String storageName = "oauthRefreshTokensCache";

    /**
     * Maximum number of active refresh tokens that an application
     * can receive. If the application requests more that this limit,
     * the request will be denied and the access token will not be issued.
     */
    private long maxActiveTokensAllowed;

    /**
     * Create access token as JWTs.
     */
    private boolean createAsJwt;

    /**
     * If true, the refresh token will keep track of the access tokens
     * issued using it. The access tokens will remain attached to the refresh token
     * as long as the refresh token is valid and they are not periodically on on-demand
     * checked for validity or removed from the refresh token if they expire.
     * The tracking mechanism is only for history and auditing purposes.
     */
    private boolean trackAccessTokens = true;
}
