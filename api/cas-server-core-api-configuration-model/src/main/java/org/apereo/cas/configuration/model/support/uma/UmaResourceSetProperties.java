package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

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
@JsonFilter("UmaResourceSetProperties")
public class UmaResourceSetProperties implements Serializable {
    private static final long serialVersionUID = 215435145313504895L;

    /**
     * Store resource-sets and policies via JPA.
     */
    @NestedConfigurationProperty
    private UmaResourceSetJpaProperties jpa = new UmaResourceSetJpaProperties();
}
