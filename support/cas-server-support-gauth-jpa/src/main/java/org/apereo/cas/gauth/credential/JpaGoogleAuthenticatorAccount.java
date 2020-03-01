package org.apereo.cas.gauth.credential;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.List;

/**
 * This is {@link JpaGoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord")
@Getter
@NoArgsConstructor
public class JpaGoogleAuthenticatorAccount extends GoogleAuthenticatorAccount {
    private static final long serialVersionUID = -4546447152725241946L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    public JpaGoogleAuthenticatorAccount(final String username, final String secretKey,
                                         final int validationCode, final List<Integer> scratchCodes) {
        super(username, secretKey, validationCode, scratchCodes);
        this.id = System.currentTimeMillis();
    }
}
