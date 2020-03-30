package com.yubico.webauthn;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.CredentialRegistration;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link RegistrationStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public interface RegistrationStorage extends CredentialRepository {

    boolean addRegistrationByUsername(String username, CredentialRegistration reg);

    Collection<CredentialRegistration> getRegistrationsByUsername(String username);

    Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray credentialId);

    Collection<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle);

    default boolean userExists(final String username) {
        return !getRegistrationsByUsername(username).isEmpty();
    }

    boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration);

    boolean removeAllRegistrations(String username);

    void updateSignatureCount(AssertionResult result);

}
