package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link NexmoProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-sms-clickatell")
@Getter
@Setter
@Accessors(chain = true)
public class NexmoProperties implements Serializable {
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
