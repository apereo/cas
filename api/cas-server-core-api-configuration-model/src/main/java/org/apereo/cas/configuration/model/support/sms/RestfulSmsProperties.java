package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link RestfulSmsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-util", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class RestfulSmsProperties extends RestEndpointProperties {
    @Serial
    private static final long serialVersionUID = -8102345678378393382L;

    /**
     * Indicate the style and formatting of the SMS request parameters
     * and how they should be included and sent via REST.
     */
    private RestfulSmsRequestStyles style = RestfulSmsRequestStyles.QUERY_PARAMETERS;

    public RestfulSmsProperties() {
        setMethod("POST");
    }

    public enum RestfulSmsRequestStyles {
        /**
         * This option will submit an SMS http request where
         * the from, to, etc are passed as request query parameters.
         * Only the message itself is included in the request body.
         */
        QUERY_PARAMETERS,
        /**
         * This option will pass all parameters in the body
         * of the SMS http request as a JSON object.
         */
        REQUEST_BODY
    }
}
