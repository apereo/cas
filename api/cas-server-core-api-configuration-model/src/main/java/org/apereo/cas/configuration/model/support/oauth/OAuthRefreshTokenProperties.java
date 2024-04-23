package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("OAuthRefreshTokenProperties")
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

}
