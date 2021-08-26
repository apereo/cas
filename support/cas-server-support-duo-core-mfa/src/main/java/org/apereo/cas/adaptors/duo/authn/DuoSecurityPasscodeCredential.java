package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DuoSecurityPasscodeCredential}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Setter
public class DuoSecurityPasscodeCredential extends OneTimePasswordCredential implements MultifactorAuthenticationCredential {
    private static final long serialVersionUID = 3007700749231783156L;

    private final String providerId;

    public DuoSecurityPasscodeCredential(final String id, final String password, final String providerId) {
        super(id, password);
        this.providerId = providerId;
    }
}
