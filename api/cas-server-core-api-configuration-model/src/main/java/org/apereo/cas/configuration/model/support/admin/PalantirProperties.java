package org.apereo.cas.configuration.model.support.admin;

import module java.base;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link PalantirProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-palantir")
@Getter
@Setter
@Accessors(chain = true)
public class PalantirProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6599875416590735492L;

    /**
     * Palantir CAS authentication properties.
     */
    private PalantirCasProperties casAuthentication = new PalantirCasProperties();

    /**
     * Directory that holds application definition templates, as JSON files, that are offered
     * when an application definition is written by hand in the dashboard, next to the templates
     * that ship with CAS. The directory is read every time the dashboard is shown, so templates
     * can be added or changed without a restart. Files that cannot be read as an application
     * definition, or whose application type is not available, are skipped.
     */
    private String templatesDirectory;

    @RequiresModule(name = "cas-server-support-palantir")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class PalantirCasProperties implements Serializable {

        @Serial
        private static final long serialVersionUID = -6912332279389902759L;

        @RequiredProperty
        private String requiredAttributeName;

        @RequiredProperty
        @RegularExpressionCapable
        private String requiredAttributeValue;
    }
}
