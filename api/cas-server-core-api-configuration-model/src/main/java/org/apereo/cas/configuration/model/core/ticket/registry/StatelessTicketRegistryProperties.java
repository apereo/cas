package org.apereo.cas.configuration.model.core.ticket.registry;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link StatelessTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-tickets", automated = true)
@Getter
@Setter

@Accessors(chain = true)
public class StatelessTicketRegistryProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2600525447128979994L;

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto = new EncryptionRandomizedSigningJwtCryptographyProperties();

    /**
     * Control how data produced by the registry would be managed and stored by the browser storage.
     * <p><br>
     * Browser storage is a web storage technology that allows CAS to store and retrieve data on a user's device. It provides a
     * way to persistently store key-value pairs in a web browser. Unlike cookies,
     * browser storage has a larger storage capacity (usually 5-10 MB per domain), and
     * the data is not sent to the server with every HTTP request, which can improve performance.
     * Browser storage is scoped to a particular CAS domain. Each CAS domain has its own separate storage,
     * and one website cannot access the storage of another domain due to the same-origin policy.
     * <p><br>
     * The following options are valid:
     *
     * <ul>
     *     <li>{@code LOCAL}: Data stored in local storage persists even when the user closes the browser or navigates
     *     away from the page. It remains available until explicitly cleared by the user or the web application.
     *     </li>
     *     <li>{@code SESSION}: Data stored in session storage is only available for the duration of the page session.
     *     It gets cleared when the user closes the browser or tab. If a user opens a new tab or window and navigates
     *     to the same page, a new session storage instance is created.</li>
     * </ul>
     */
    private String storageType = "LOCAL";

    public StatelessTicketRegistryProperties() {
        crypto.setEnabled(true);
        crypto.setSigningEnabled(false);
        crypto.getEncryption().setKeySize(EncryptionRandomizedSigningJwtCryptographyProperties.DEFAULT_ENCRYPTION_KEY_SIZE);
    }
}
