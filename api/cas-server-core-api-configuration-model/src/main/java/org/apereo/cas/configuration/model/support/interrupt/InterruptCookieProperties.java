package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link InterruptCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("InterruptCookieProperties")
public class InterruptCookieProperties extends PinnableCookieProperties {
    private static final long serialVersionUID = -266090748600049578L;

    /**
     * Decide if cookie paths should be automatically configured
     * based on the application context path, when the cookie
     * path is not configured.
     */
    private boolean autoConfigureCookiePath = true;

    public InterruptCookieProperties() {
        super.setName("CASINTERRUPT");
    }
}
