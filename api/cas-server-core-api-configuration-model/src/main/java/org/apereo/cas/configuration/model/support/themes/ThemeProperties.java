package org.apereo.cas.configuration.model.support.themes;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ThemeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-themes", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ThemeProperties")
public class ThemeProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2248773823196496599L;

    /**
     * The default theme name of this CAS deployment.
     * The default theme file {@code cas-theme-default.properties} can be modified
     * and extended by the theme file {@code cas-theme-custom.properties}.
     */
    private String defaultThemeName = "cas-theme-default";

    /**
     * The parameter name used to switch themes.
     */
    private String paramName = "theme";
}
