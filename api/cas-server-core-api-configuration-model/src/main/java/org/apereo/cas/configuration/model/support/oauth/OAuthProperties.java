package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.replication.CookieSessionReplicationProperties;
import org.apereo.cas.configuration.model.support.replication.SessionReplicationProperties;
import org.apereo.cas.configuration.model.support.uma.UmaProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 2677128037234123907L;


    /**
     * Control settings for session replication.
     */
    @NestedConfigurationProperty
    private SessionReplicationProperties sessionReplication = new SessionReplicationProperties();

    /**
     * Control the CSRF cookie settings in OAUTH authentication flows.
     */
    @NestedConfigurationProperty
    private OAuthCsrfCookieProperties csrfCookie = new OAuthCsrfCookieProperties();

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
     * Settings related to oauth core behavior.
     */
    @NestedConfigurationProperty
    private OAuthCoreProperties core = new OAuthCoreProperties();

    public OAuthProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        if (StringUtils.isBlank(getSessionReplication().getCookie().getName())) {
            getSessionReplication().getCookie().setName("%s%s".formatted(
                CookieSessionReplicationProperties.DEFAULT_COOKIE_NAME, "OauthOidcServerSupport"));
        }
    }
}
