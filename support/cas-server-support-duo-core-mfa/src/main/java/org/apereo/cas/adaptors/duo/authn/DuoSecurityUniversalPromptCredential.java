package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.credential.AbstractCredential;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * This is {@link DuoSecurityDirectCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Setter
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "token", callSuper = true)
public class DuoSecurityUniversalPromptCredential extends AbstractCredential implements MultifactorAuthenticationCredential {
    private static final long serialVersionUID = -7571699733132111037L;

    private final String token;

    private final Authentication authentication;

    private String providerId;

    @Override
    public String getId() {
        return this.token;
    }
}
