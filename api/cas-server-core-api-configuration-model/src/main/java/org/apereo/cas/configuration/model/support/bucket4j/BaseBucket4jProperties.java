package org.apereo.cas.configuration.model.support.bucket4j;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
