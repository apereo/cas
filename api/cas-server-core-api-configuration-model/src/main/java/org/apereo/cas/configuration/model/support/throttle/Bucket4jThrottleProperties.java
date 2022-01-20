package org.apereo.cas.configuration.model.support.throttle;

import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
@JsonFilter("Bucket4jThrottleProperties")
public class Bucket4jThrottleProperties extends BaseBucket4jProperties {
    private static final long serialVersionUID = 5813165633105563813L;
}
