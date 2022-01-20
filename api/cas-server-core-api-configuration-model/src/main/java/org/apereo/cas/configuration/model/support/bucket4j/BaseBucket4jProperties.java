package org.apereo.cas.configuration.model.support.bucket4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
@JsonFilter("BaseBucket4jProperties")
public abstract class BaseBucket4jProperties implements Serializable {
    private static final long serialVersionUID = 1813165633105563813L;

    /**
     * Decide whether bucket4j functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether the request should block until capacity becomes available.
     * Consume a token from the token bucket. If a token is not available this
     * will block until the refill adds one to the bucket.
     */
    private boolean blocking = true;

    /**
     * Describe the available bandwidth and the overall limitations.
     * Multiple bandwidths allow for different policies per unit of measure.
     * (i.e. allows 1000 tokens per 1 minute, but not often then 50 tokens per 1 second).
     */
    private List<Bucket4jBandwidthLimitProperties> bandwidth = new ArrayList<>();
}
