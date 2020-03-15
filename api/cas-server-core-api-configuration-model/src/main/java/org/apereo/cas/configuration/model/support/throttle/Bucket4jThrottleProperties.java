package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for cas.throttle.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-throttle-bucket4j")
@Getter
@Setter
@Accessors(chain = true)
public class Bucket4jThrottleProperties implements Serializable {
    private static final long serialVersionUID = 5813165633105563813L;

    /**
     * Number of tokens that can be used within the time window.
     */
    private int capacity = 120;

    /**
     * Indicate the overdraft used if requests are above the average capacity.
     * A positive value activates a greedy strategy for producing tokens for capacity.
     */
    private int overdraft;

    /**
     * Time window in which capacity can be allowed.
     */
    private int rangeInSeconds = 60;

    /**
     * Whether the request should block until capacity becomes available.
     */
    private boolean blocking = true;
}
