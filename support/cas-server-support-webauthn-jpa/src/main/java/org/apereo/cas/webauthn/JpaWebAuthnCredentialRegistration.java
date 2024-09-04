package org.apereo.cas.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link JpaWebAuthnCredentialRegistration}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@SuperBuilder
@Entity
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class JpaWebAuthnCredentialRegistration implements Serializable {
    /**
     * JPA entity name.
     */
    static final String ENTITY_NAME = "JpaWebAuthnCredentialRegistration";

    @Serial
    private static final long serialVersionUID = 1505204109111619367L;

    @Id
    @JsonProperty("id")
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "webauthn_sequence")
    @SequenceGenerator(name = "webauthn_sequence", allocationSize = 100)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Lob
    @Column(name = "records", length = Integer.MAX_VALUE)
    private String records;
}
