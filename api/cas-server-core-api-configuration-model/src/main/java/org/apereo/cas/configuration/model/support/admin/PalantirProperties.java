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
