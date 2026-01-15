package org.apereo.cas.configuration.model.support.gcp;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link GoogleCloudFirestoreTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-support-gcp-firestore-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class GoogleCloudFirestoreTicketRegistryProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 8243690796900322918L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto =
        new EncryptionRandomizedSigningJwtCryptographyProperties().setEnabled(false);

}
