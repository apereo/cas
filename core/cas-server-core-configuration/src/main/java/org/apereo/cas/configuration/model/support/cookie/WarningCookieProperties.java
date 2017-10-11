package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.support.RequiredModule;

/**
 * Configuration properties class for warn.cookie.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiredModule(name = "cas-server-support-cookie", automated = true)
public class WarningCookieProperties extends CookieProperties {

    private static final long serialVersionUID = -266090748600049578L;

    public WarningCookieProperties() {
        super.setName("CASPRIVACY");
    }
}
