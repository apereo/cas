package org.apereo.cas.configuration.model.support.themes;

import org.apereo.cas.configuration.support.RequiresModule;

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
public class ThemeProperties implements Serializable {

    /**
     * The default theme name of this CAS deployment.
     */
    public static final String DEFAULT_THEME_NAME = "cas-theme-default";
    
    @Serial
    private static final long serialVersionUID = 2248773823196496599L;
    
    /**
     * The default theme name of this CAS deployment.
     * The default theme file {@code cas-theme-default.properties} can be modified
     * and extended by the theme file {@code cas-theme-custom.properties}.
     */
    private String defaultThemeName = DEFAULT_THEME_NAME;

    /**
     * The parameter name used to switch themes.
     */
    private String paramName = "theme";
}
