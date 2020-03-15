package org.apereo.cas.configuration.model.core.logging;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link LoggingProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-logging", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class LoggingProperties implements Serializable {

    private static final long serialVersionUID = 7455171260665661949L;

    /**
     * Allow CAS to add http request details into the logging's MDC filter.
     * Mapped Diagnostic Context is essentially a map maintained by the logging
     * framework where the application code provides key-value pairs which can then be
     * inserted by the logging framework in log messages. MDC data can also be highly
     * helpful in filtering messages or triggering certain actions.
     */
    private boolean mdcEnabled = true;
}
