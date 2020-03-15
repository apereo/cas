package org.apereo.cas.configuration.model.core.authentication;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link TimeBasedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class TimeBasedAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 3826749727400569308L;

    /**
     * The mfa provider id that should be triggered.
     */
    private String providerId;

    /**
     * Trigger mfa after this hour, specified in 24-hour format.
     */
    private long onOrAfterHour = 20;

    /**
     * Trigger mfa before this hour, specified in 24-hour format.
     */
    private long onOrBeforeHour = 7;

    /**
     * Trigger mfa on the following days of the week.
     */
    private List<String> onDays = new ArrayList<>(0);
}
