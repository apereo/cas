package org.apereo.cas.support.saml.metadata.resolver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is {@link MongoDbMetadata}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Document
public class MongoDbMetadata {
    @Id
    private String id;

    @Indexed
    private String name;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
