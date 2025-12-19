package org.apereo.cas.configuration.model.support.pulsar;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PulsarTicketRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-pulsar-ticket-registry")
@Getter
@Setter
@Accessors(chain = true)
public class PulsarTicketRegistryProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 2222012359713238647L;

    private String subscriptionName = "cas-pulsar-ticket-registry-subscription";

    private int concurrency = 1;
}
