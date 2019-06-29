package org.apereo.cas.webauthn.credential;

import org.apereo.cas.webauthn.util.SerializableByteArray;

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
    boolean addRegistrationByUsername(String username, CredentialRegistrationRequest reg);

    Collection<CredentialRegistrationRequest> getRegistrationsByUsername(String username);

    Optional<CredentialRegistrationRequest> getRegistrationByUsernameAndCredentialId(String username, ByteArray userHandle);

    Collection<CredentialRegistrationRequest> getRegistrationsByUserHandle(ByteArray userHandle);

    boolean removeRegistrationByUsername(String username, CredentialRegistrationRequest credentialRegistration);

    boolean removeAllRegistrations(String username);

    void updateSignatureCount(AssertionResult result);
}
