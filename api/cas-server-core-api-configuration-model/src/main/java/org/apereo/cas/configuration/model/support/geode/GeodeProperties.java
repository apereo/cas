package org.apereo.cas.configuration.model.support.geode;

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link GeodeProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiresModule(name = "cas-server-support-geode-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class GeodeProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = -1259465262649559156L;

    /**
     * When a Geode member starts up, it contacts one or more locators. The locator(s) maintain a list
     * of all live members and help new members discover one another. They manage the membership view
     * and broadcast updates when nodes join or leave the cluster.
     * The locator acts as a bootstrap service. A member that does not yet know about the rest of
     * the cluster can reach out to the locator to obtain the current membership list and configuration
     * details. This is particularly useful when multicast is disabled (commonly the case in many environments),
     * and static locator configuration is required.
     * <p>Locators continuously monitor the health of the cluster members. If a member becomes unresponsive, the
     * locator updates the membership view, which can trigger re-balancing or other recovery actions.
     * Specify one or more locators in {@code host[port]} format.
     * Multiple locators can be comma separated. A blank value or {@code none} disables the locator support.
     * <p>
     * You typically disable multicast by settings its port to zero to rely exclusively on locators for
     * discovery. This ensures that all members explicitly contact the known locator addresses.
     * <p>For a real cluster, you’ll usually start locators as separate
     * processes using Geode’s command-line tool, {@code gfsh}.
     */
    private String locators = "localhost[10334]";

    /**
     * Members broadcast their presence over the multicast group so that any new or
     * existing member on the same network can detect them automatically.
     * The multicast mechanism helps build the initial membership view of the cluster
     * and continuously updates it as nodes join or leave, which
     * simplifies the network configuration in environments where nodes are
     * frequently added or removed.
     * You typically disable multicast by settings its port to zero.
     */
    private int multicastPort;
    
    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    private EncryptionRandomizedSigningJwtCryptographyProperties crypto =
        new EncryptionRandomizedSigningJwtCryptographyProperties().setEnabled(false);
}
