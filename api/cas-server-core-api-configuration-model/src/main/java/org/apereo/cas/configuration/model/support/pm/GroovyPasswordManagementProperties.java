package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GroovyPasswordManagementProperties")
public class GroovyPasswordManagementProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 8079027843747126083L;
}
