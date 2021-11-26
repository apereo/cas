package org.apereo.cas.oidc.jwks.generator;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * This is {@link OidcJsonWebKeystoreEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "OidcJpaJsonWebKeystore")
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class OidcJsonWebKeystoreEntity implements Serializable {
    private static final long serialVersionUID = -6371242034035828803L;

    @Id
    @org.springframework.data.annotation.Id
    @Column(nullable = false, length = 1024)
    private String issuer;

    @Column(nullable = false, length = Integer.MAX_VALUE)
    @Lob
    private String data;

}
