package org.apereo.cas.configuration.model.support.cookie;

/**
 * Configuration properties class for warn.cookie.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class WarningCookieProperties extends CookieProperties {

    private static final long serialVersionUID = -266090748600049578L;

    public WarningCookieProperties() {
        super.setName("CASPRIVACY");
    }
}
