package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.credential.AbstractCredential;
import org.apereo.cas.authentication.principal.Principal;

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
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DuoSecurityDirectCredential extends AbstractCredential implements MultifactorAuthenticationCredential {
    private static final long serialVersionUID = -7570699733132111037L;

    private final Principal principal;

    private final String providerId;

    @Override
    public String getId() {
        return getPrincipal().getId();
    }
}
