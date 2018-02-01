package org.apereo.cas.configuration.model.support.oauth;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OAuthProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Slf4j
@Getter
@Setter
public class OAuthProperties implements Serializable {

    private static final long serialVersionUID = 2677128037234123907L;

    /**
     * Profile view types.
     */
    public enum UserProfileViewTypes {

        /**
         * Return and render the user profile view in nested mode.
         * This is the default option, most usually.
         */
        NESTED, /**
         * Return and render the user profile view in flattened mode
         * where all attributes are flattened down to one level only.
         */
        FLAT
    }

    /**
     * Settings related to oauth grants.
     */
    private OAuthGrantsProperties grants = new OAuthGrantsProperties();

    /**
     * Settings related to oauth codes.
     */
    private OAuthCodeProperties code = new OAuthCodeProperties();

    /**
     * Settings related to oauth access tokens.
     */
    private OAuthAccessTokenProperties accessToken = new OAuthAccessTokenProperties();

    /**
     * Settings related to oauth refresh tokens.
     */
    private OAuthRefreshTokenProperties refreshToken = new OAuthRefreshTokenProperties();

    /**
     * User profile view type determines how the final user profile should be rendered
     * once an access token is "validated". 
     */
    private UserProfileViewTypes userProfileViewType = UserProfileViewTypes.NESTED;
}
