package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;
import java.math.BigInteger;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is {@link JpaGoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord",
    uniqueConstraints = @UniqueConstraint(columnNames = {"username", "name"}))
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@NoArgsConstructor
public class JpaGoogleAuthenticatorAccount extends GoogleAuthenticatorAccount {
    @Serial
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
            .username(acct.getUsername().trim().toLowerCase(Locale.ENGLISH))
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes()
                .stream()
                .map(code -> BigInteger.valueOf(code.longValue()))
                .collect(Collectors.toList()))
            .registrationDate(acct.getRegistrationDate())
            .name(acct.getName())
            .build();
    }
}
