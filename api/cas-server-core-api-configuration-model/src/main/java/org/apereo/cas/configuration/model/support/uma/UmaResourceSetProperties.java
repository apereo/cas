package org.apereo.cas.configuration.model.support.uma;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link UmaResourceSetProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Setter
@Accessors(chain = true)
public class UmaResourceSetProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 215435145313504895L;

    /**
     * Store resource-sets and policies via JPA.
     */
    @NestedConfigurationProperty
    private UmaResourceSetJpaProperties jpa = new UmaResourceSetJpaProperties();
}
