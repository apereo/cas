package org.apereo.cas.impl.token;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This is {@link JpaPasswordlessAuthenticationEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "PasswordlessAuthenticationToken")
@Getter
@SuperBuilder
@NoArgsConstructor
public class JpaPasswordlessAuthenticationEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -6830552508331229032L;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 2048)
    private String token;

    @Column(name = "EXP_DATE", length = Integer.MAX_VALUE, nullable = false)
    private ZonedDateTime expirationDate;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Builder.Default
    private long id = -1;
}
