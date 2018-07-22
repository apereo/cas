package org.apereo.cas;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class MongoDbProperty {

    @Id
    private String id;

    @Indexed
    private String name;

    private Object value;
}
