package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * This is {@link JpaGoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord",
    uniqueConstraints = @UniqueConstraint(columnNames = { "username", "name" }))
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@NoArgsConstructor
public class JpaGoogleAuthenticatorAccount extends GoogleAuthenticatorAccount {
    private static final long serialVersionUID = -4546447152725241946L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Builder.Default
    private long id = -1;

    /**
     * Update account info from account object.
     *
     * @param acct to be updated
     * @return this
     */
    public static JpaGoogleAuthenticatorAccount from(final OneTimeTokenAccount acct) {
        return JpaGoogleAuthenticatorAccount.builder()
            .id(acct.getId())
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .registrationDate(acct.getRegistrationDate())
            .name(acct.getName())
            .build();
    }
}
