package org.apereo.cas.configuration.model.support.bucket4j;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Configuration properties class for bucket4j.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-bucket4j-core")
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseBucket4jProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 1813165633105563813L;

    /**
     * Decide whether bucket4j functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether the request should block until capacity becomes available.
     * When enabled, a request that finds the bucket empty parks its thread until
     * the refill adds a token. On a request-serving path that turns a burst into
     * thread-pool exhaustion, so throttling rejects the request immediately by
     * default and only waits when this is explicitly turned on.
     */
    private boolean blocking = true;

    /**
     * Describe the available bandwidth and the overall limitations.
     * Multiple bandwidths allow for different policies per unit of measure.
     * (i.e. allows 1000 tokens per 1 minute, but not often then 50 tokens per 1 second).
     */
    private List<Bucket4jBandwidthLimitProperties> bandwidth = new ArrayList<>();
}
