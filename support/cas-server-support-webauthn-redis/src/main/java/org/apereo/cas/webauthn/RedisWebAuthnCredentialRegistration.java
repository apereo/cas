package org.apereo.cas.webauthn;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link RedisWebAuthnCredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Accessors(chain = true)
public class RedisWebAuthnCredentialRegistration implements Serializable {
    @Serial
    private static final long serialVersionUID = 1505204109111619367L;

    @JsonProperty
    private String username;

    @JsonProperty
    private String records;
}
