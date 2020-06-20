package org.apereo.cas.gauth.credential;

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
    private static final long serialVersionUID = 2441775052626253711L;
}
