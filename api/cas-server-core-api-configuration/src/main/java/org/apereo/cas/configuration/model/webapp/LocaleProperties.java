package org.apereo.cas.configuration.model.webapp;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for locale.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web")
@Slf4j
@Getter
@Setter
public class LocaleProperties implements Serializable {

    private static final long serialVersionUID = -1644471820900213781L;

    /**
     * Parameter name to use when switching locales.
     */
    private String paramName = "locale";

    /**
     * Default locale.
     */
    private String defaultValue = "en";
}
