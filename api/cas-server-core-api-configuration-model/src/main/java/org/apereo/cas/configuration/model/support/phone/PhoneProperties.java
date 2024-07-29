package org.apereo.cas.configuration.model.support.phone;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PhoneProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)

public class PhoneProperties implements CasFeatureModule, Serializable {

    @Serial
    private static final long serialVersionUID = -3713886839517507306L;

    /**
     * The body of the phone call message.
     */
    @RequiredProperty
    private String text;

    /**
     * The from address for the message.
     */
    @RequiredProperty
    private String from;

    /**
     * Principal attribute name that indicates the destination phone number
     * for this voice message. The attribute must already be resolved and available
     * to the CAS principal.
     */
    @RequiredProperty
    private String attributeName = "phone";
}
