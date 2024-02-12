package org.apereo.cas.configuration.model.core.logging;

import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

/**
 * This is {@link MdcLoggingProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiresModule(name = "cas-server-core-logging", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class MdcLoggingProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 2609192595133868519L;

    /**
     * Allow CAS to add http request details into the logging's MDC filter.
     * Mapped Diagnostic Context is essentially a map maintained by the logging
     * framework where the application code provides key-value pairs which can then be
     * inserted by the logging framework in log messages. MDC data can also be highly
     * helpful in filtering messages or triggering certain actions.
     */
    private boolean enabled = true;

    /**
     * A list of parameters to exclude.
     * This list is used to specify parameters that should be excluded from MDC logging.
     * The parameter names can be specified as regular expressions.
     */
    @RegularExpressionCapable
    private List<String> parametersToExclude = Stream.of(".*password.*").toList();

    /**
     * A list of headers to exclude.
     * This list is used to specify parameters that should be excluded from MDC logging.
     * The parameter names can be specified as regular expressions.
     */
    @RegularExpressionCapable
    private List<String> headersToExclude = Stream.of("cookie", "authorization").toList();
}
