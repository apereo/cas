package org.apereo.cas.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * This is {@link MongoDbWebAuthnCredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Document
@NoArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class MongoDbWebAuthnCredentialRegistration implements Serializable {
    /**
     * username field.
     */
    public static final String FIELD_USERNAME = "username";

    /**
     * records field.
     */
    public static final String FIELD_RECORDS = "records";

    private static final long serialVersionUID = 1505204109111619367L;

    @Id
    private String id;

    @JsonProperty
    private String username;

    @JsonProperty
    private String records;
}
