package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link GoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
public class GoogleAuthenticatorAccount extends OneTimeTokenAccount {
    @Serial
    private static final long serialVersionUID = 2441775052626253711L;

    /**
     * From one time token account into gauth account.
     *
     * @param acct the acct
     * @return the google authenticator account
     */
    public static GoogleAuthenticatorAccount from(final OneTimeTokenAccount acct) {
        return builder()
            .id(acct.getId())
            .name(acct.getName())
            .username(acct.getUsername())
            .secretKey(acct.getSecretKey())
            .validationCode(acct.getValidationCode())
            .scratchCodes(acct.getScratchCodes())
            .registrationDate(acct.getRegistrationDate())
            .source(acct.getSource())
            .build();
    }

    @Override
    public String getSource() {
        return "Google Authenticator";
    }
}
