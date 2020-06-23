package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link GoogleAuthenticatorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@NoArgsConstructor(force = true)
@Getter
@Setter
@ToString(callSuper = true)
public class GoogleAuthenticatorTokenCredential extends OneTimeTokenCredential {

    private static final long serialVersionUID = -7570600701132111037L;

    private Long accountId;

    public GoogleAuthenticatorTokenCredential(final String token, final Long accountId) {
        super(token);
        setAccountId(accountId);
    }
}
