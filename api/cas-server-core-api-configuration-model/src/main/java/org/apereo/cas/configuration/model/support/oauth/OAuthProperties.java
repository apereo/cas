package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.uma.UmaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

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
     * Control the CSRF cookie settings in OAUTH authentication flows.
     */
    @NestedConfigurationProperty
    private CsrfCookieProperties csrfCookie = new CsrfCookieProperties();

    /**
     * Crypto settings that sign/encrypt secrets.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

    /**
     * Settings related to oauth grants.
     */
    @NestedConfigurationProperty
    private OAuthGrantsProperties grants = new OAuthGrantsProperties();
    /**
     * Settings related to oauth codes.
     */
    @NestedConfigurationProperty
    private OAuthCodeProperties code = new OAuthCodeProperties();
    /**
     * Settings related to oauth access tokens.
     */
    @NestedConfigurationProperty
    private OAuthAccessTokenProperties accessToken = new OAuthAccessTokenProperties();
    /**
     * Settings related to oauth refresh tokens.
     */
    @NestedConfigurationProperty
    private OAuthRefreshTokenProperties refreshToken = new OAuthRefreshTokenProperties();
    /**
     * Settings related to oauth device tokens.
     */
    @NestedConfigurationProperty
    private OAuthDeviceTokenProperties deviceToken = new OAuthDeviceTokenProperties();
                                                                        
    /**
     * Settings related to oauth device user codes.
     */
    @NestedConfigurationProperty
    private OAuthDeviceUserCodeProperties deviceUserCode = new OAuthDeviceUserCodeProperties();

    /**
     * OAuth UMA authentication settings.
     */
    @NestedConfigurationProperty
    private UmaProperties uma = new UmaProperties();

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

    public OAuthProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
