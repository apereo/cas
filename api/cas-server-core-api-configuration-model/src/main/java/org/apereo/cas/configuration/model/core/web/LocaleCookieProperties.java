package org.apereo.cas.configuration.model.core.web;

import module java.base;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link LocaleCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class LocaleCookieProperties extends CookieProperties {
    @Serial
    private static final long serialVersionUID = 158577966798914031L;
}
