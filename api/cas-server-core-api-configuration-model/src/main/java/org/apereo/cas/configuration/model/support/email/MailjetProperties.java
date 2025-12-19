package org.apereo.cas.configuration.model.support.email;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MailjetProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-mailjet", automated = false)
@Accessors(chain = true)
public class MailjetProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2504930082372661815L;

    /**
     * API key used to authenticate to the mailjet service.
     */
    @ExpressionLanguageCapable
    @RequiredProperty
    private String apiKey;

    /**
     * Secret key used to authenticate to the mailjet service.
     */
    @ExpressionLanguageCapable
    @RequiredProperty
    private String secretKey;


    /**
     * Bearer access token used to authenticate to the mailjet service.
     * Mainly requires for the SMS integration.
     */
    @ExpressionLanguageCapable
    @RequiredProperty
    private String bearerAccessToken;
    /**
     * Indicates whether the mailjet integration should be run in sandbox mode.
     * By setting this property to a {@code true} value, you will turn off the delivery of
     * the message while still getting back the full range of error messages that could be related to
     * your message processing. If the message is processed without error, the response will follow the
     * normal response payload format, omitting only the {@code MessageID} and {@code MessageUUID}.
     */
    private boolean sandboxMode;
}
