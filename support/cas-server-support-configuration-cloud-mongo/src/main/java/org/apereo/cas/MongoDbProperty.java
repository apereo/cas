package org.apereo.cas;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * This is {@link MongoDbProperty}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Document
@Getter
@Setter
@EqualsAndHashCode
public class MongoDbProperty implements Serializable {

    private static final long serialVersionUID = -8152946700415601078L;
    
    @Id
    private String id;

    @Indexed
    private String name;

    private Object value;
}
