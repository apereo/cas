package org.apereo.cas.configuration.model.support.sms;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TwilioProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiresModule(name = "cas-server-support-twilio")
@Getter
@Accessors(chain = true)
@Setter

public class TwilioProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -7043132225482495229L;

    /**
     * Twilio account identifier used for authentication.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String accountId;

    /**
     * Twilio secret token used for authentication.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String token;

    /**
     * Whether the module is enabled or not, defaults to true.
     */
    @RequiredProperty
    private boolean enabled = true;

    /**
     * Controls whether Twilio support should also handle making phone calls.
     */
    private boolean phoneCallsEnabled;
}
