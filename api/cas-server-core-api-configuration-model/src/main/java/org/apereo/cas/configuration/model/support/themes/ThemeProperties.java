package org.apereo.cas.configuration.model.support.themes;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ThemeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-themes", automated = true)
@Slf4j
@Getter
@Setter
public class ThemeProperties implements Serializable {

    private static final long serialVersionUID = 2248773823196496599L;

    /**
     * The default theme name of this CAS deployment.
     */
    private String defaultThemeName = "cas-theme-default";

    /**
     * The parameter name used to switch themes.
     */
    private String paramName = "theme";
}
