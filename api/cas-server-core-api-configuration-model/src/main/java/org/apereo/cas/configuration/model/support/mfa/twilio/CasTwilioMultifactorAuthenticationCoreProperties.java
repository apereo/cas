package org.apereo.cas.configuration.model.support.mfa.twilio;

import module java.base;
import org.apereo.cas.configuration.model.support.sms.TwilioAccountProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CasTwilioMultifactorAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-twilio-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CasTwilioMultifactorAuthenticationCoreProperties extends TwilioAccountProperties {
    @Serial
    private static final long serialVersionUID = 8993327424439747161L;

    /**
     * The identifier of the verification service typically in the {@code ^VA[0-9a-fA-F]{32}$} format.
     * A Verification Service is the set of common configurations used to create and check verifications.
     * You can create a service with the API or in the Console.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String serviceSid;

    /**
     * The attribute name that contains the contact info (phone, etc) to send the verification attempt.
     */
    @RequiredProperty
    private String recipientAttributeName = "phoneNumber";

    /**
     * The list of channels to use for verification.
     * Supported valued are: {@code sms, call, email, whatsapp, sna}.
     * Default is {@code sms}.
     */
    private List<String> verificationChannels = Stream.of("sms").toList();
}
