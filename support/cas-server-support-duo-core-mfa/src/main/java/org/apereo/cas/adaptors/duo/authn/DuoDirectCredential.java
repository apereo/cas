package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.AbstractCredential;
import org.apereo.cas.authentication.Authentication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DuoDirectCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Setter
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DuoDirectCredential extends AbstractCredential {
    private static final long serialVersionUID = -7570699733132111037L;

    private final Authentication authentication;

    @Override
    public String getId() {
        return this.authentication.getPrincipal().getId();
    }
}
