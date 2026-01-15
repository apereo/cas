package org.apereo.cas.bucket4j.consumer;

import module java.base;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link BucketConsumptionResult}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
public class BucketConsumptionResult implements Serializable {
    @Serial
    private static final long serialVersionUID = -3289639572775949915L;

    private final boolean consumed;

    private final long tokensRemaining;

    private final long retryAfterSeconds;

    @Builder.Default
    private final Map<String, String> headers = new LinkedHashMap<>();
}
