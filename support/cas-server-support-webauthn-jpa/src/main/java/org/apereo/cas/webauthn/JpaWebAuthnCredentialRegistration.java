package org.apereo.cas.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Lob;
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
public class JpaWebAuthnCredentialRegistration implements Serializable {
    private static final long serialVersionUID = 1505204109111619367L;

    @Id
    @Builder.Default
    @JsonProperty("id")
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(nullable = false, unique = true)
    private String username;

    @Lob
    @Column(name = "records", length = Integer.MAX_VALUE)
    private String records;

    public JpaWebAuthnCredentialRegistration() {
        setId(System.nanoTime());
    }

}
