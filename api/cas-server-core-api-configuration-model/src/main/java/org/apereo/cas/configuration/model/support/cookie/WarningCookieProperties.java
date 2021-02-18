package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configuration properties class for warn.cookie.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-cookie", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WarningCookieProperties")
public class WarningCookieProperties extends PinnableCookieProperties {
    private static final long serialVersionUID = -266090748600049578L;

    /**
     * Decide if cookie paths should be automatically configured
     * based on the application context path, when the cookie
     * path is not configured.
     */
    private boolean autoConfigureCookiePath = true;

    public WarningCookieProperties() {
        super.setName("CASPRIVACY");
    }
}
