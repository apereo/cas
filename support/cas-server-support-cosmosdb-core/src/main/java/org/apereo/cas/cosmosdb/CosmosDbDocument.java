package org.apereo.cas.cosmosdb;

import com.microsoft.azure.spring.data.documentdb.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is {@link CosmosDbDocument}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Document
public class CosmosDbDocument {
    @Id
    private String id;
    
    @PartitionKey
    private String partitionKey;
    
    private String body;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(final String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
