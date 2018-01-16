package org.apereo.cas.configuration.model.support.sms;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link TwilioProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-sms-twilio")
@Slf4j
@Getter
@Setter
public class TwilioProperties implements Serializable {

    private static final long serialVersionUID = -7043132225482495229L;

    /**
     * Twilio account identifier used for authentication.
     */
    @RequiredProperty
    private String accountId;

    /**
     * Twilio secret token used for authentication.
     */
    @RequiredProperty
    private String token;
}
