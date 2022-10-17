package org.apereo.cas.configuration.model.support.mfa.duo;

import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationRegistrationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-duo")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("DuoSecurityMultifactorAuthenticationRegistrationProperties")
public class DuoSecurityMultifactorAuthenticationRegistrationProperties extends BaseMultifactorAuthenticationProviderProperties {
    @Serial
    private static final long serialVersionUID = -1655375354167880807L;

    /**
     * Crypto settings on duo registration payloads and redirects to the url.
     */
    @NestedConfigurationProperty
    private EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto =
        new EncryptionOptionalSigningOptionalJwtCryptographyProperties();

    /**
     * Link to a registration app, typically developed in-house
     * in order to allow new users to sign-up for duo functionality.
     * If the user account status requires enrollment and this link
     * is specified, CAS will redirect the authentication flow
     * to this registration app. Otherwise, the default duo mechanism
     * for new-user registrations shall take over.
     * Upon redirecting to the registration app, CAS would also build
     * a {@code principal} parameter into the registration URL, typically
     * in form of a JSON web token that conveys the user's identity. This JWT
     * can be signed and/or encrypted using settings defined via the {@link #getCrypto()}
     * configuration block here.
     */
    private String registrationUrl;

    public DuoSecurityMultifactorAuthenticationRegistrationProperties() {
        crypto.setEnabled(false);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
