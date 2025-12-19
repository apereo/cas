package com.yubico.core;

import module java.base;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.data.ByteArray;

public interface RegistrationStorage extends CredentialRepository {

    boolean addRegistrationByUsername(String username, CredentialRegistration reg);

    Collection<CredentialRegistration> getRegistrationsByUsername(String username);

    Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(String username, ByteArray credentialId);

    Collection<CredentialRegistration> getRegistrationsByUserHandle(ByteArray userHandle);

    default boolean userExists(final String username) {
        return !getRegistrationsByUsername(username).isEmpty();
    }

    boolean removeRegistrationByUsername(String username, CredentialRegistration credentialRegistration);

    boolean removeRegistrationByUsernameAndCredentialId(String username, ByteArray credentialRegistration);

    boolean removeAllRegistrations(String username);

    void updateSignatureCount(AssertionResult result);

}
