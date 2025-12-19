package org.apereo.cas.configuration.model.core.audit;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link AuditGroovyProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiresModule(name = "cas-server-core-audit", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class AuditGroovyProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 4887475246873585918L;

    /**
     * Groovy template that constructs the audit payload.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties template = new SpringResourceProperties();
}
