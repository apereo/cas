package org.apereo.cas.cosmosdb;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link CosmosDbDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
public class CosmosDbDocument implements Serializable {
    private static final long serialVersionUID = -8481978861727839081L;

    private String id;

    private String serviceId;

    private String body;
}
