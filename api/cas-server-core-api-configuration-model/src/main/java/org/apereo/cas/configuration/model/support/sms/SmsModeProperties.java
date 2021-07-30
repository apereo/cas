package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SmsModeProperties}.
 *
 * @author Jérôme Rautureau
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-sms-smsmode")
@Getter
@Setter
@Accessors(chain = true)
public class SmsModeProperties implements Serializable {

  private static final long serialVersionUID = -4185702036613030013L;

    /**
     * Secure token used to establish a handshake with the service.
     */
    @RequiredProperty
    private String accessToken;

    /**
     * query attribute name for the message
     */
    private String messageAttributeName = "message";

    /**
     * query attribute name for the to
     */
    private String toAttributeName = "numero";

    /**
     * URL to contact and send messages (GET only).
     */
    @RequiredProperty
    private String sendMessageUrl = "https://api.smsmode.com/http/1.6/sendSMS.do";

    /**
     * Headers, defined as a Map, to include in the request when making the HTTP call.
     * Will overwrite any header that CAS is pre-defined to
     * send and include in the request. Key in the map should be the header name
     * and the value in the map should be the header value.
     */
    private Map<String, String> headers = new HashMap<>();
}
