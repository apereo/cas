package org.apereo.cas.gauth.token;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JpaGoogleAuthenticatorToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorToken")
public class JpaGoogleAuthenticatorToken extends GoogleAuthenticatorToken {
    @Serial
    private static final long serialVersionUID = 9047539820264192234L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @SuppressWarnings("UnusedVariable")
    private long id = -1;
}
