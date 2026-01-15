package org.apereo.cas.configuration.model.support.sms;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link SmsModeProperties}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-sms-smsmode")
@Getter
@Setter
@Accessors(chain = true)
public class SmsModeProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -4185702036613030013L;

    /**
     * Secure token used to establish a handshake with the service.
     */
    @RequiredProperty
    private String accessToken;

    /**
     * URL to contact and send messages (POST only).
     */
    @RequiredProperty
    private String url = "https://rest.smsmode.com/sms/v1/messages";

    /**
     * URL of the proxy (if defined).
     */
    private String proxyUrl;
}
