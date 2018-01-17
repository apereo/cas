package org.apereo.cas.cosmosdb;

import com.microsoft.azure.spring.data.documentdb.core.mapping.PartitionKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;

/**
 * This is {@link CosmosDbDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Document
@Slf4j
@Getter
public class CosmosDbDocument {

    @Id
    private String id;

    @PartitionKey
    private String partitionKey;

    private String body;

    public void setId(final String id) {
        this.id = id;
    }

    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
