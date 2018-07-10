package org.apereo.cas.cosmosdb;

import com.microsoft.azure.spring.data.documentdb.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

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
