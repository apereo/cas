package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedCryptoProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link GoogleAuthenticatorMultifactorScratchCodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiresModule(name = "cas-server-support-gauth")
@Getter
@Setter
@Accessors(chain = true)

public class GoogleAuthenticatorMultifactorScratchCodeProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 8740203143088539401L;

    /**
     * Settings that deal with encryption of values.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedCryptoProperties encryption = new EncryptionRandomizedCryptoProperties();

}
