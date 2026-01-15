package org.apereo.cas.webauthn;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link DynamoDbWebAuthnCredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class DynamoDbWebAuthnCredentialRegistration implements Serializable {

    @Serial
    private static final long serialVersionUID = 4557023022194574325L;

    @JsonProperty
    private String username;

    @JsonProperty
    private List<String> records;
}
