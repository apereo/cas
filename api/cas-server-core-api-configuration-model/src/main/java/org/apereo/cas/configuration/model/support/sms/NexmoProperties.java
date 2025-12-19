package org.apereo.cas.configuration.model.support.sms;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link NexmoProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-sms-nexmo")
@Getter
@Setter
@Accessors(chain = true)
public class NexmoProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 7546596773588579321L;

    /**
     * Nexmo API token obtained from Nexmo.
     */
    @RequiredProperty
    private String apiToken;

    /**
     * Nexmo API secret obtained from Nexmo.
     */
    private String apiSecret;

    /**
     * Nexmo Signature secret obtained from Nexmo.
     */
    private String signatureSecret;

}
