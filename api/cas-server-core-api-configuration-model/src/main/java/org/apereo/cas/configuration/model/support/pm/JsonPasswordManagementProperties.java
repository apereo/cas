package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("JsonPasswordManagementProperties")
public class JsonPasswordManagementProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 1129426669588789974L;
}
