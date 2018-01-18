package org.apereo.cas.cosmosdb;

import com.microsoft.azure.spring.data.documentdb.core.mapping.PartitionKey;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Getter
@Setter
public class CosmosDbDocument {

    @Id
    private String id;

    @PartitionKey
    private String partitionKey;

    private String body;
}
