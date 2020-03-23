package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link OAuthProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Accessors(chain = true)
@Setter
public class OAuthProperties implements Serializable {

    private static final long serialVersionUID = 2677128037234123907L;

    /**
     * Indicates whether profiles and other session data,
     * collected as part of OAuth flows and requests
     * that are kept by the container session, should be replicated
     * across the cluster using CAS and its own ticket registry.
     * Without this option, OAuth profile data and other related
     * pieces of information should be manually replicated
     * via means and libraries outside of CAS.
     */
    private boolean replicateSessions;

    /**
     * Crypto settings that sign/encrypt secrets.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

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
     * Settings related to oauth device tokens.
     */
    private OAuthDeviceTokenProperties deviceToken = new OAuthDeviceTokenProperties();

    /**
     * Settings related to oauth device user codes.
     */
    private OAuthDeviceUserCodeProperties deviceUserCode = new OAuthDeviceUserCodeProperties();

    /**
     * User profile view type determines how the final user profile should be rendered
     * once an access token is "validated".
     */
    private UserProfileViewTypes userProfileViewType = UserProfileViewTypes.NESTED;

    /**
     * Profile view types.
     */
    public enum UserProfileViewTypes {

        /**
         * Return and render the user profile view in nested mode.
         * This is the default option, most usually.
         */
        NESTED,
        /**
         * Return and render the user profile view in flattened mode
         * where all attributes are flattened down to one level only.
         */
        FLAT
    }
}
