package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link JsonPasswordManagementProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm")
@Getter
@Setter
@Accessors(chain = true)
public class JsonPasswordManagementProperties extends SpringResourceProperties {
    @Serial
    private static final long serialVersionUID = 1129426669588789974L;
}
