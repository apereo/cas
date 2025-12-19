package org.apereo.cas.cosmosdb;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CosmosDbDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 8.0.0
 */
@Getter
@Setter
@Deprecated(since = "8.0.0", forRemoval = true)
public class CosmosDbDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -8481978861727839081L;

    private String id;

    private String serviceId;

    private String body;
}
