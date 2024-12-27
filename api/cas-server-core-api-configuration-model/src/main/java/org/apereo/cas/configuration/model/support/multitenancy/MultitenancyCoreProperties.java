package org.apereo.cas.configuration.model.support.multitenancy;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link MultitenancyCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-core-multitenancy")
@Getter
@Setter
@Accessors(chain = true)
public class MultitenancyCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 7884847182894875156L;

    /**
     * Enable multitenancy support.
     */
    private boolean enabled;
}
