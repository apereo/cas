package org.apereo.cas.gauth.token;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private long id = -1;
}
