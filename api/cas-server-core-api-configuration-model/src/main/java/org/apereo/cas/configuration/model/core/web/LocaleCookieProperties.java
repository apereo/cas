package org.apereo.cas.configuration.model.core.web;

import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("LocaleCookieProperties")
public class LocaleCookieProperties extends PinnableCookieProperties implements Serializable {
    private static final long serialVersionUID = 158577966798914031L;
}
