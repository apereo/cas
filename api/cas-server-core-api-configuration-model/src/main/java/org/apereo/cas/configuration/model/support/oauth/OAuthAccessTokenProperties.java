package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OAuthAccessTokenProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-oauth")
@Getter
@Setter
@Accessors(chain = true)
public class OAuthAccessTokenProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -6832081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT8H";

    /**
     * Sliding window for the access token expiration policy.
     * Essentially, this is an idle time out.
     */
    @DurationCapable
    private String timeToKillInSeconds = "PT2H";

    /**
     * Create access token as JWTs.
     */
    private boolean createAsJwt;

    /**
     * Whether CAS should include extra CAS attributes as claims in the JWT access token.
     * This setting is only relevant if the access token is a determined to be a JWT.
     */
    private boolean includeClaimsInJwt = true;

    /**
     * The storage object name used and created by CAS to hold OAuth access tokens
     * in the backing ticket registry implementation.
     */
    private String storageName = "oauthAccessTokensCache";

    /**
     * Maximum number of active access tokens that an application
     * can receive. If the application requests more that this limit,
     * the request will be denied and the access token will not be issued.
     */
    private long maxActiveTokensAllowed;

    /**
     * Crypto settings.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

    public OAuthAccessTokenProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
