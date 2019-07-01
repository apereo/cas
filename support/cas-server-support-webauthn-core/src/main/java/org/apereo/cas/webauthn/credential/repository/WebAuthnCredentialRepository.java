package org.apereo.cas.webauthn.credential.repository;

import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;

import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.data.ByteArray;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link WebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface WebAuthnCredentialRepository extends CredentialRepository {
    boolean addRegistrationByUsername(String username, WebAuthnCredentialRegistration reg);

    Collection<WebAuthnCredentialRegistration> getRegistrationsByUsername(String username);

    Optional<WebAuthnCredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray userHandle);

    Collection<WebAuthnCredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle);

    boolean removeRegistrationByUsername(String username, WebAuthnCredentialRegistration credentialRegistration);

    boolean removeAllRegistrations(String username);

    void updateSignatureCount(AssertionResult result);
}
