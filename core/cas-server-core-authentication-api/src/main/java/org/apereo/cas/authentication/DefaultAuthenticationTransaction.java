package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serial;
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
@Getter
@RequiredArgsConstructor
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class DefaultAuthenticationTransaction implements RegisteredServiceAwareAuthenticationTransaction {

    @Serial
    private static final long serialVersionUID = 6213904009424725484L;

    private final Service service;

    private final RegisteredService registeredService;

    private final Collection<Credential> credentials;

    private final Collection<Authentication> authentications = new ArrayList<>();

    @Override
    @CanIgnoreReturnValue
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

