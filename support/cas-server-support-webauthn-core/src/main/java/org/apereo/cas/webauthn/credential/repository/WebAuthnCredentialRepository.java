package org.apereo.cas.webauthn.credential;

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
    boolean addRegistrationByUsername(String username, CredentialRegistration reg);

    Collection<CredentialRegistration> getRegistrationsByUsername(String username);

    Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray userHandle);

    Collection<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle);

    boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration);

    boolean removeAllRegistrations(String username);

    void updateSignatureCount(AssertionResult result);
}
