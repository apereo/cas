package org.apereo.cas.bucket4j.consumer;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link BucketConsumptionResult}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
public class BucketConsumptionResult implements Serializable {
    private static final long serialVersionUID = -3289639572775949915L;

    private final boolean consumed;

    @Builder.Default
    private final Map<String, String> headers = new LinkedHashMap<>();
}
