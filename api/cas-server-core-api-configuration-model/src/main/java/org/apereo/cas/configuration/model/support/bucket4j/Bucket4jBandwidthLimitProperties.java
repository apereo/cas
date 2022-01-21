package org.apereo.cas.configuration.model.support.bucket4j;

import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link Bucket4jBandwidthLimitProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-bucket4j-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("Bucket4jBandwidthLimitProperties")
public class Bucket4jBandwidthLimitProperties implements Serializable {
    private static final long serialVersionUID = -4208702997065904970L;

    /**
     * By default initial size of bucket equals to capacity.
     * But sometimes, you may want to have lesser initial size,
     * for example for case of cold start in order to prevent denial of service.
     */
    private long initialTokens;

    /**
     * Number of tokens/requests that can be used within the time window.
     */
    private long capacity = 120;

    /**
     * The number of tokens that should be used to refill the bucket
     * given the specified refill duration.
     */
    private long refillCount = 10;

    /**
     * Time window in which capacity can be allowed.
     */
    @DurationCapable
    private String duration = "PT60S";

    /**
     * Duration to use to refill the bucket.
     */
    @DurationCapable
    private String refillDuration = "PT30S";

    /**
     * Describes how the bucket should be refilled.
     * Specifies the speed of tokens regeneration.
     */
    private BandwidthRefillStrategies refillStrategy = BandwidthRefillStrategies.GREEDY;

    /**
     * Describe options available for refill strategy.
     */
    public enum BandwidthRefillStrategies {
        /**
         * This type of refill regenerates tokens in a greedy manner; it tries to add the
         * tokens to bucket as soon as possible. For example refill "10 tokens per 1 second"
         * adds 1 token per each 100 millisecond; in other words refill will
         * not wait 1 second to regenerate 10 tokens.
         */
        GREEDY,
        /**
         * This type of refill regenerates tokens in intervally manner. "Intervally"
         * in opposite to "greedy" will wait until whole period would be
         * elapsed before regenerating tokens.
         */
        INTERVALLY
    }
}
