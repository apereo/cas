package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link DefaultAuthenticationTransaction}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Getter(onMethod = @__({@Override}))
@RequiredArgsConstructor
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class DefaultAuthenticationTransaction implements AuthenticationTransaction {

    private static final long serialVersionUID = 6213904009424725484L;

    private final Service service;

    private final Collection<Credential> credentials;

    private final Collection<Authentication> authentications = new ArrayList<>();

    @Override
    public AuthenticationTransaction collect(final Collection<Authentication> authentications) {
        this.authentications.addAll(authentications);
        return this;
    }

    @Override
    public Optional<Credential> getPrimaryCredential() {
        return Objects.requireNonNull(credentials).stream().findFirst();
    }

    @Override
    public boolean hasCredentialOfType(final Class<? extends Credential> type) {
        return Objects.requireNonNull(credentials).stream().anyMatch(type::isInstance);
    }
}

