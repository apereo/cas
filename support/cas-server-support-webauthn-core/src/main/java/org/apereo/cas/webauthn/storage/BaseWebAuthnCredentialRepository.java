package org.apereo.cas.webauthn.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link BaseWebAuthnCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseWebAuthnCredentialRepository implements WebAuthnCredentialRepository {

    private final CasConfigurationProperties properties;

    private final CipherExecutor<String, String> cipherExecutor;

    @Override
    public boolean addRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        val registrations = getRegistrationsByUsername(username);
        registrations.add(credentialRegistration);
        update(username, new HashSet<>(registrations));
        return true;
    }

    @Override
    public Optional<CredentialRegistration> getRegistrationByUsernameAndCredentialId(final String username, final ByteArray id) {
        val registrations = getRegistrationsByUsername(username);
        return registrations.stream().filter(credReg -> id.equals(credReg.getCredential().getCredentialId())).findFirst();
    }

    @Override
    public Collection<CredentialRegistration> getRegistrationsByUserHandle(final ByteArray handle) {
        return stream()
            .filter(credentialRegistration -> handle.equals(credentialRegistration.getUserIdentity().getId()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean removeRegistrationByUsername(final String username, final CredentialRegistration credentialRegistration) {
        val registrations = getRegistrationsByUsername(username);
        val result = registrations.remove(credentialRegistration);
        update(username, new HashSet<>(registrations));
        return result;
    }

    @Override
    public boolean removeAllRegistrations(final String username) {
        update(username, new HashSet<>());
        return true;
    }

    @Override
    public void updateSignatureCount(final AssertionResult result) {
        val username = result.getUsername();
        val registration = getRegistrationByUsernameAndCredentialId(username, result.getCredentialId())
            .orElseThrow(() -> new NoSuchElementException(String.format("Credential \"%s\" is not registered to user \"%s\"",
                result.getCredentialId(), username)));
        val registrations = getRegistrationsByUsername(username);
        registrations.remove(registration);
        registrations.add(registration.withSignatureCount(result.getSignatureCount()));
        update(username, new HashSet<>(registrations));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(final String username) {
        return getRegistrationsByUsername(username).stream()
            .map(registration -> PublicKeyCredentialDescriptor
                .builder()
                .id(registration.getCredential().getCredentialId())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(final String username) {
        return getRegistrationsByUsername(username)
            .stream()
            .findAny()
            .map(reg -> reg.getUserIdentity().getId());
    }

    @Override
    public Optional<String> getUsernameForUserHandle(final ByteArray userHandle) {
        return getRegistrationsByUserHandle(userHandle).stream().findAny().map(CredentialRegistration::getUsername);
    }

    @Override
    public Optional<RegisteredCredential> lookup(final ByteArray credentialId, final ByteArray userHandle) {
        val registration = stream()
            .filter(Objects::nonNull)
            .filter(credReg -> credentialId.equals(credReg.getCredential().getCredentialId()))
            .findAny();

        return registration.flatMap(reg -> Optional.of(RegisteredCredential.builder()
            .credentialId(reg.getCredential().getCredentialId())
            .userHandle(reg.getUserIdentity().getId())
            .publicKeyCose(reg.getCredential().getPublicKeyCose())
            .signatureCount(reg.getSignatureCount())
            .build()));
    }

    @Override
    public Set<RegisteredCredential> lookupAll(final ByteArray credentialId) {
        return stream()
            .filter(Objects::nonNull)
            .filter(reg -> reg.getCredential().getCredentialId().equals(credentialId))
            .map(reg -> RegisteredCredential.builder()
                .credentialId(reg.getCredential().getCredentialId())
                .userHandle(reg.getUserIdentity().getId())
                .publicKeyCose(reg.getCredential().getPublicKeyCose())
                .signatureCount(reg.getSignatureCount())
                .build())
            .collect(Collectors.toSet());
    }

    @Override
    public void clean() {
        try {
            val webAuthn = properties.getAuthn().getMfa().getWebAuthn().getCore();
            val expirationDate = LocalDate.now(ZoneOffset.UTC)
                .minus(webAuthn.getExpireDevices(), DateTimeUtils.toChronoUnit(webAuthn.getExpireDevicesTimeUnit()));
            LOGGER.debug("Filtering devices based on device expiration date [{}]", expirationDate);

            val expInstant = expirationDate.atStartOfDay(ZoneOffset.UTC).toInstant();
            val removingDevices = stream()
                .filter(Objects::nonNull)
                .filter(d -> d.getRegistrationTime() != null && d.getRegistrationTime().isBefore(expInstant))
                .collect(Collectors.toList());
            if (!removingDevices.isEmpty()) {
                LOGGER.debug("There are [{}] expired device(s) remaining in repository. Cleaning...", removingDevices.size());
                removingDevices.forEach(device -> removeRegistrationByUsername(device.getUsername(), device));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    /**
     * Update records by user.
     *
     * @param username the username
     * @param records  the records
     */
    protected abstract void update(String username, Collection<CredentialRegistration> records);
}
