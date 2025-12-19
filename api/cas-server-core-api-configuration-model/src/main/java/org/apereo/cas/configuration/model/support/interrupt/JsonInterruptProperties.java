package org.apereo.cas.configuration.model.support.interrupt;

import module java.base;
import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JsonInterruptProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-interrupt-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class JsonInterruptProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 1079027840047126083L;
}
