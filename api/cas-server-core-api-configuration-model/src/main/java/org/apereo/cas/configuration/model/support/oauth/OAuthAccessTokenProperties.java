package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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

    private static final long serialVersionUID = -6832081675586528350L;

    /**
     * Hard timeout to kill the access token and expire it.
     */
    private String maxTimeToLiveInSeconds = "PT28800S";

    /**
     * Sliding window for the access token expiration policy.
     * Essentially, this is an idle time out.
     */
    private String timeToKillInSeconds = "PT7200S";

    /**
     * Create access token as JWTs.
     */
    private boolean createAsJwt;

    /**
     * Crypto settings.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

    public OAuthAccessTokenProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
