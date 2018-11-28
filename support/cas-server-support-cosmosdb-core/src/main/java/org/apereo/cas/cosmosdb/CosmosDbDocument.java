package org.apereo.cas.cosmosdb;

import com.microsoft.azure.spring.data.documentdb.core.mapping.Document;
import com.microsoft.azure.spring.data.documentdb.core.mapping.PartitionKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

/**
 * This is {@link CosmosDbDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Document
@Getter
@Setter
public class CosmosDbDocument {

    @Id
    private String id;

    @PartitionKey
    private String partitionKey;

    private String body;
}
