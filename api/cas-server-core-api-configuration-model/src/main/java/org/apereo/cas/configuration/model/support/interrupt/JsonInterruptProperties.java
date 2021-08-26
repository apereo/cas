package org.apereo.cas.configuration.model.support.interrupt;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("JsonInterruptProperties")
public class JsonInterruptProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 1079027840047126083L;
}
