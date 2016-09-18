package org.apereo.cas;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This is {@link MongoDbProperty}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Document
public class MongoDbProperty {
    @Id
    private String id;
    
    @Indexed
    private String name;
    
    private Object value;

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

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }
}

