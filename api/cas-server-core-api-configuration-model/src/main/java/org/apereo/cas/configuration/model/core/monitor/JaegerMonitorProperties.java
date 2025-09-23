package org.apereo.cas.configuration.model.core.monitor;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link JaegerMonitorProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-tracing-jaeger")
@Getter
@Setter
@Accessors(chain = true)
public class JaegerMonitorProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2526137275423093529L;

    /**
     * The GRPC endpoint to use when sending traces to the Jaeger server.
     */
    private String endpoint = "http://localhost:4317";

    /**
     * The timeout to use when making requests to the Jaeger server.
     */
    private Duration timeout = Duration.ofSeconds(5);

    /**
     * Specifies the duration to wait for a connection to be established before timing out.
     * This timeout applies to the initial connection phase and does not impact
     * data transfer once the connection is established.
     */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /**
     * Accepted values are: {@code IMMUTABLE_DATA}, {@code MUTABLE_DATA}.
     */
    private String memoryMode = "IMMUTABLE_DATA";

    /**
     * Maximum number of retry attempts.
     */
    private int maxRetryAttempts = 3;

    /**
     * A map of custom headers to be included in requests.
     */
    private Map<String, String> headers = new HashMap<>();
}
