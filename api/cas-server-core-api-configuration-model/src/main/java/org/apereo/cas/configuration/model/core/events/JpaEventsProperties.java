package org.apereo.cas.configuration.model.core.events;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link JpaEventsProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-events-jpa")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("JpaEventsProperties")
public class JpaEventsProperties extends AbstractJpaProperties {
    @Serial
    private static final long serialVersionUID = 7647381223153797806L;

    /**
     * Whether capturing events via JPA is enabled.
     */
    @RequiredProperty
    private boolean enabled = true;
}
